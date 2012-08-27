<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>KPEI</title>
<link href="${pageContext.request.contextPath}/themes/default/login.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div id="outer">
  <div id="header">
    <div id="banner"></div>
    <div id="logo"></div>
  </div>
  <div id="content">
    <div id="login">
      <form method="post" action="${pageContext.request.contextPath}/login.j" id="switchform">
        <h1>SIGN IN</h1>
        <div id="reason">${reason}</div>
        <ul class="form">
          <li>
            <label>User ID</label>
            <input name="j_username" type="text" size="15" />
          </li>
          <li>
            <label>Password</label>
            <input name="j_password" type="password" size="15" />
          </li>
        </ul>
      	<div id="divbutton">
          <div><input type="submit" name="btn_Login" class="btn" value="Login" /></div>
        </div>
      </form>
    </div>
  </div>
</div>
<div id="footer">copyright&copy; 2011</div>
</body>
</html>
