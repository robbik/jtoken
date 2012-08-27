/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package mo.org.jboss.netty.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import mo.org.jboss.netty.util.internal.ConcurrentIdentityHashMap;
import mo.org.jboss.netty.util.internal.ReusableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link Timer} optimized for approximated I/O timeout scheduling.
 * 
 * <h3>Tick Duration</h3>
 * 
 * As described with 'approximated', this timer does not execute the scheduled
 * {@link TimerTask} on time. {@link HashedWheelTimer}, on every tick, will
 * check if there are any {@link TimerTask}s behind the schedule and execute
 * them.
 * <p>
 * You can increase or decrease the accuracy of the execution timing by
 * specifying smaller or larger tick duration in the constructor. In most
 * network applications, I/O timeout does not need to be accurate. Therefore,
 * the default tick duration is 100 milliseconds and you will not need to try
 * different configurations in most cases.
 * 
 * <h3>Ticks per Wheel (Wheel Size)</h3>
 * 
 * {@link HashedWheelTimer} maintains a data structure called 'wheel'. To put
 * simply, a wheel is a hash table of {@link TimerTask}s whose hash function is
 * 'dead line of the task'. The default number of ticks per wheel (i.e. the size
 * of the wheel) is 512. You could specify a larger value if you are going to
 * schedule a lot of timeouts.
 * 
 * <h3>Do not create many instances.</h3>
 * 
 * {@link HashedWheelTimer} creates a new thread whenever it is instantiated and
 * started. Therefore, you should make sure to create only one instance and
 * share it across your application. One of the common mistakes, that makes your
 * application unresponsive, is to create a new instance in
 * {@link ChannelPipelineFactory}, which results in the creation of a new thread
 * for every connection.
 * 
 * <h3>Implementation Details</h3>
 * 
 * {@link HashedWheelTimer} is based on <a
 * href="http://cseweb.ucsd.edu/users/varghese/">George Varghese</a> and Tony
 * Lauck's paper, <a
 * href="http://cseweb.ucsd.edu/users/varghese/PAPERS/twheel.ps.Z">'Hashed and
 * Hierarchical Timing Wheels: data structures to efficiently implement a timer
 * facility'</a>. More comprehensive slides are located <a
 * href="http://www.cse.wustl.edu/~cdgill/courses/cs6874/TimingWheels.ppt"
 * >here</a>.
 * 
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2297 $, $Date: 2010-06-07 10:50:02 +0900 (Mon, 07 Jun 2010) $
 */
public class HashedWheelTimer implements Timer {

	private static final Logger logger = LoggerFactory.getLogger(HashedWheelTimer.class);

	private static final AtomicInteger id = new AtomicInteger();

	private final Worker worker = new Worker();
	final Thread workerThread;
	final AtomicBoolean shutdown = new AtomicBoolean();

	private final long roundDuration;
	final long tickDuration;
	final Set<HashedWheelTimeout>[] wheel;
	final ReusableIterator<HashedWheelTimeout>[] iterators;
	final int mask;
	final ReadWriteLock lock = new ReentrantReadWriteLock();
	volatile int wheelCursor;
	
	final Map<String, HashedWheelTimeout> timeouts;
	
	public HashedWheelTimer() {
		this(true);
	}

	/**
	 * Creates a new timer with the default thread factory (
	 * {@link Executors#defaultThreadFactory()}), default tick duration, and
	 * default number of ticks per wheel.
	 */
	public HashedWheelTimer(boolean daemon) {
		this(daemon, Executors.defaultThreadFactory());
	}

	/**
	 * Creates a new timer with the default thread factory (
	 * {@link Executors#defaultThreadFactory()}) and default number of ticks per
	 * wheel.
	 * 
	 * @param tickDuration
	 *            the duration between tick
	 * @param unit
	 *            the time unit of the {@code tickDuration}
	 */
	public HashedWheelTimer(boolean daemon, long tickDuration, TimeUnit unit) {
		this(daemon, Executors.defaultThreadFactory(), tickDuration, unit);
	}

	/**
	 * Creates a new timer with the default thread factory (
	 * {@link Executors#defaultThreadFactory()}).
	 * 
	 * @param tickDuration
	 *            the duration between tick
	 * @param unit
	 *            the time unit of the {@code tickDuration}
	 * @param ticksPerWheel
	 *            the size of the wheel
	 */
	public HashedWheelTimer(boolean daemon, long tickDuration, TimeUnit unit, int ticksPerWheel) {
		this(daemon, Executors.defaultThreadFactory(), tickDuration, unit,
				ticksPerWheel);
	}

	/**
	 * Creates a new timer with the default tick duration and default number of
	 * ticks per wheel.
	 * 
	 * @param threadFactory
	 *            a {@link ThreadFactory} that creates a background
	 *            {@link Thread} which is dedicated to {@link TimerTask}
	 *            execution.
	 */
	public HashedWheelTimer(boolean daemon, ThreadFactory threadFactory) {
		this(daemon, threadFactory, 100, TimeUnit.MILLISECONDS);
	}

	/**
	 * Creates a new timer with the default number of ticks per wheel.
	 * 
	 * @param threadFactory
	 *            a {@link ThreadFactory} that creates a background
	 *            {@link Thread} which is dedicated to {@link TimerTask}
	 *            execution.
	 * @param tickDuration
	 *            the duration between tick
	 * @param unit
	 *            the time unit of the {@code tickDuration}
	 */
	public HashedWheelTimer(boolean daemon, ThreadFactory threadFactory, long tickDuration,
			TimeUnit unit) {
		this(daemon, threadFactory, tickDuration, unit, 512);
	}

	/**
	 * Creates a new timer.
	 * 
	 * @param threadFactory
	 *            a {@link ThreadFactory} that creates a background
	 *            {@link Thread} which is dedicated to {@link TimerTask}
	 *            execution.
	 * @param tickDuration
	 *            the duration between tick
	 * @param unit
	 *            the time unit of the {@code tickDuration}
	 * @param ticksPerWheel
	 *            the size of the wheel
	 */
	public HashedWheelTimer(boolean daemon, ThreadFactory threadFactory, long tickDuration,
			TimeUnit unit, int ticksPerWheel) {

		if (threadFactory == null) {
			throw new NullPointerException("threadFactory");
		}
		if (unit == null) {
			throw new NullPointerException("unit");
		}
		if (tickDuration <= 0) {
			throw new IllegalArgumentException(
					"tickDuration must be greater than 0: " + tickDuration);
		}
		if (ticksPerWheel <= 0) {
			throw new IllegalArgumentException(
					"ticksPerWheel must be greater than 0: " + ticksPerWheel);
		}

		// Normalize ticksPerWheel to power of two and initialize the wheel.
		wheel = createWheel(ticksPerWheel);
		iterators = createIterators(wheel);
		mask = wheel.length - 1;

		// Convert tickDuration to milliseconds.
		this.tickDuration = tickDuration = unit.toMillis(tickDuration);

		// Prevent overflow.
		if ((tickDuration == Long.MAX_VALUE)
				|| (tickDuration >= Long.MAX_VALUE / wheel.length)) {
			throw new IllegalArgumentException("tickDuration is too long: "
					+ tickDuration + ' ' + unit);
		}

		roundDuration = tickDuration * wheel.length;

		workerThread = threadFactory.newThread(worker);
		workerThread.setName("Hashed wheel timer #" + id.incrementAndGet());
		workerThread.setDaemon(daemon);
		
		timeouts = new HashMap<String, HashedWheelTimeout>();
	}

	@SuppressWarnings("unchecked")
	private static Set<HashedWheelTimeout>[] createWheel(int ticksPerWheel) {
		if (ticksPerWheel <= 0) {
			throw new IllegalArgumentException(
					"ticksPerWheel must be greater than 0: " + ticksPerWheel);
		}
		if (ticksPerWheel > 1073741824) {
			throw new IllegalArgumentException(
					"ticksPerWheel may not be greater than 2^30: "
							+ ticksPerWheel);
		}

		ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);

		Set<HashedWheelTimeout>[] wheel = new Set[ticksPerWheel];
		for (int i = 0; i < wheel.length; i++) {
			wheel[i] = new MapBackedSet<HashedWheelTimeout>(
					new ConcurrentIdentityHashMap<HashedWheelTimeout, Boolean>(
							16, 0.95f, 4));
		}

		return wheel;
	}

	@SuppressWarnings("unchecked")
	private static ReusableIterator<HashedWheelTimeout>[] createIterators(
			Set<HashedWheelTimeout>[] wheel) {
		ReusableIterator<HashedWheelTimeout>[] iterators = new ReusableIterator[wheel.length];
		for (int i = 0; i < wheel.length; i++) {
			iterators[i] = (ReusableIterator<HashedWheelTimeout>) wheel[i]
					.iterator();
		}

		return iterators;
	}

	private static int normalizeTicksPerWheel(int ticksPerWheel) {
		int normalizedTicksPerWheel = 1;
		while (normalizedTicksPerWheel < ticksPerWheel) {
			normalizedTicksPerWheel <<= 1;
		}

		return normalizedTicksPerWheel;
	}

	/**
	 * Starts the background thread explicitly. The background thread will start
	 * automatically on demand even if you did not call this method.
	 * 
	 * @throws IllegalStateException
	 *             if this timer has been {@linkplain #stop() stopped} already
	 */
	public synchronized void start() {
		if (shutdown.get()) {
			throw new IllegalStateException("cannot be started once stopped");
		}

		if (!workerThread.isAlive()) {
			workerThread.start();
		}
	}

	public synchronized Set<Timeout> stop() {
		if (Thread.currentThread() == workerThread) {
			throw new IllegalStateException(
					HashedWheelTimer.class.getSimpleName()
							+ ".stop() cannot be called from "
							+ TimerTask.class.getSimpleName());
		}

		if (!shutdown.compareAndSet(false, true)) {
			return Collections.emptySet();
		}

		boolean interrupted = false;
		while (workerThread.isAlive()) {
			workerThread.interrupt();
			try {
				workerThread.join(100);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}

		if (interrupted) {
			Thread.currentThread().interrupt();
		}

		Set<Timeout> unprocessedTimeouts = new HashSet<Timeout>();
		for (Set<HashedWheelTimeout> bucket : wheel) {
			unprocessedTimeouts.addAll(bucket);
			bucket.clear();
		}
		
		synchronized (timeouts) {
			timeouts.clear();
		}

		return Collections.unmodifiableSet(unprocessedTimeouts);
	}

	public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
		return newTimeout(null, task, delay, unit);
	}
	
	public Timeout newTimeout(String taskId, TimerTask task, long delay, TimeUnit unit) {
		final long currentTime = System.currentTimeMillis();

		if (task == null) {
			throw new NullPointerException("task");
		}
		if (unit == null) {
			throw new NullPointerException("unit");
		}

		if (!workerThread.isAlive()) {
			start();
		}

		delay = unit.toMillis(delay);
		
		HashedWheelTimeout timeout = new HashedWheelTimeout(taskId, task, currentTime + delay);
		scheduleTimeout(timeout, delay);
		
		return timeout;
	}
	
	public Timeout newTimeout(TimerTask task, long deadline) {
		return newTimeout(null, task, deadline);
	}

	public Timeout newTimeout(String taskId, TimerTask task, long deadline) {
		final long currentTime = System.currentTimeMillis();

		if (task == null) {
			throw new NullPointerException("task");
		}

		if (currentTime > deadline) {
			return null;
		}

		if (!workerThread.isAlive()) {
			start();
		}

		HashedWheelTimeout timeout = new HashedWheelTimeout(taskId, task, deadline);
		scheduleTimeout(timeout, deadline - currentTime);
		
		return timeout;
	}
	
	public Timeout cancelTimeout(String taskId) {
		if (taskId == null) {
			return null;
		}
		
		HashedWheelTimeout timeout;
		
		synchronized(timeouts) {
			timeout = timeouts.get(taskId);
		}
		
		if (timeout != null) {
			timeout.cancel();
		}
		
		return timeout;
	}

	public boolean hasTimeout() {
		boolean retval = true;

		lock.readLock().lock();
		try {
			for (int i = 0, n = wheel.length; i < n; ++i) {
				if (!wheel[i].isEmpty()) {
					retval = false;
					break;
				}
			}
		} finally {
			lock.readLock().unlock();
		}

		return retval;
	}

	private void scheduleTimeout(HashedWheelTimeout timeout, long delay) {
		// delay must be equal to or greater than tickDuration so that the
		// worker thread never misses the timeout.
		if (delay < tickDuration) {
			delay = tickDuration;
		}

		// Prepare the required parameters to schedule the timeout object.
		final long lastRoundDelay = delay % roundDuration;
		final long lastTickDelay = delay % tickDuration;
		final long relativeIndex = lastRoundDelay / tickDuration
				+ (lastTickDelay != 0 ? 1 : 0);

		final long remainingRounds = delay / roundDuration
				- (delay % roundDuration == 0 ? 1 : 0);

		if (timeout.taskId != null) {
			synchronized(timeouts) {
				timeouts.put(timeout.taskId, timeout);
			}
		}

		// Add the timeout to the wheel.
		lock.readLock().lock();
		try {
			int stopIndex = (int) (wheelCursor + relativeIndex & mask);
			timeout.stopIndex = stopIndex;
			timeout.remainingRounds = remainingRounds;
			
			wheel[stopIndex].add(timeout);
		} finally {
			lock.readLock().unlock();
		}
	}

	private final class Worker implements Runnable {

		private long startTime;
		private long tick;

		Worker() {
			super();
		}

		public void run() {
			List<HashedWheelTimeout> expiredTimeouts = new ArrayList<HashedWheelTimeout>();

			startTime = System.currentTimeMillis();
			tick = 1;

			while (!shutdown.get()) {
				final long deadline = waitForNextTick();
				if (deadline > 0) {
					fetchExpiredTimeouts(expiredTimeouts, deadline);
					notifyExpiredTimeouts(expiredTimeouts);
				}
			}
		}

		private void fetchExpiredTimeouts(
				List<HashedWheelTimeout> expiredTimeouts, long deadline) {

			// Find the expired timeouts and decrease the round counter
			// if necessary. Note that we don't send the notification
			// immediately to make sure the listeners are called without
			// an exclusive lock.
			lock.writeLock().lock();
			try {
				int newWheelCursor = wheelCursor = wheelCursor + 1 & mask;
				ReusableIterator<HashedWheelTimeout> i = iterators[newWheelCursor];
				fetchExpiredTimeouts(expiredTimeouts, i, deadline);
			} finally {
				lock.writeLock().unlock();
			}
		}

		private void fetchExpiredTimeouts(
				List<HashedWheelTimeout> expiredTimeouts,
				ReusableIterator<HashedWheelTimeout> i, long deadline) {

			List<HashedWheelTimeout> slipped = null;
			i.rewind();
			while (i.hasNext()) {
				HashedWheelTimeout timeout = i.next();
				if (timeout.remainingRounds <= 0) {
					i.remove();
					if (timeout.deadline <= deadline) {
						expiredTimeouts.add(timeout);
					} else {
						// Handle the case where the timeout is put into a wrong
						// place, usually one tick earlier. For now, just add
						// it to a temporary list - we will reschedule it in a
						// separate loop.
						if (slipped == null) {
							slipped = new ArrayList<HashedWheelTimer.HashedWheelTimeout>();
						}
						slipped.add(timeout);
					}
				} else {
					timeout.remainingRounds--;
				}
			}

			// Reschedule the slipped timeouts.
			if (slipped != null) {
				for (HashedWheelTimeout timeout : slipped) {
					scheduleTimeout(timeout, timeout.deadline - deadline);
				}
			}
		}

		private void notifyExpiredTimeouts(
				List<HashedWheelTimeout> expiredTimeouts) {
			// Notify the expired timeouts.
			for (int i = expiredTimeouts.size() - 1; i >= 0; i--) {
				expiredTimeouts.get(i).expire();
			}

			// Clean up the temporary list.
			expiredTimeouts.clear();
		}

		private long waitForNextTick() {
			long deadline = startTime + tickDuration * tick;

			for (;;) {
				final long currentTime = System.currentTimeMillis();
				final long sleepTime = tickDuration * tick
						- (currentTime - startTime);

				if (sleepTime <= 0) {
					break;
				}

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					if (shutdown.get()) {
						return -1;
					}
				}
			}

			// Increase the tick.
			tick++;
			return deadline;
		}
	}

	private final class HashedWheelTimeout implements Timeout {

		private final TimerTask task;
		
		final String taskId;

		final long deadline;

		volatile int stopIndex;
		volatile long remainingRounds;

		private volatile boolean cancelled;

		HashedWheelTimeout(String taskId, TimerTask task, long deadline) {
			this.taskId = taskId;
			this.task = task;
			
			this.deadline = deadline;
		}

		public Timer getTimer() {
			return HashedWheelTimer.this;
		}

		public TimerTask getTask() {
			return task;
		}

		public long getDeadline() {
			return deadline;
		}

		public void cancel() {
			if (isExpired()) {
				return;
			}
			
			cancelled = true;
			
			if (taskId != null) {
				synchronized (timeouts) {
					timeouts.remove(taskId);
				}
			}

			// Might be called more than once, but doesn't matter.
			wheel[stopIndex].remove(this);
		}

		public boolean isCanceled() {
			return cancelled;
		}

		public boolean isExpired() {
			return cancelled || (System.currentTimeMillis() > deadline);
		}

		public void expire() {
			if (cancelled) {
				return;
			}

			if (taskId != null) {
				synchronized (timeouts) {
					timeouts.remove(taskId);
				}
			}

			try {
				task.run(this);
			} catch (Throwable t) {
				logger.error("An exception was thrown by " + task.getClass().getSimpleName() + ".", t);
			}
		}

		@Override
		public String toString() {
			long currentTime = System.currentTimeMillis();
			long remaining = deadline - currentTime;

			StringBuilder buf = new StringBuilder(192);
			buf.append(getClass().getSimpleName());
			buf.append('(');

			buf.append("deadline: ");
			if (remaining > 0) {
				buf.append(remaining);
				buf.append(" ms later, ");
			} else if (remaining < 0) {
				buf.append(-remaining);
				buf.append(" ms ago, ");
			} else {
				buf.append("now, ");
			}

			if (isCanceled()) {
				buf.append(", cancelled");
			}

			return buf.append(')').toString();
		}
	}
}
