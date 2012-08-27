package rk.experiment.c2dm.controller;

import javax.annotation.Resource;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebLoginFormController {

    @Resource(name = "messageSource")
    private MessageSource message;

    @RequestMapping(value = "/login.html")
    public ModelAndView open(@RequestParam(value = "reason", required = false, defaultValue = "") String reason) {
        ModelAndView mnv = new ModelAndView("login");

        if ("auth".equals(reason)) {
            mnv.addObject("reason",
                    message.getMessage("login.reason.auth", new Object[0], "", LocaleContextHolder.getLocale()));
        } else if ("expired".equals(reason)) {
            mnv.addObject("reason",
                    message.getMessage("login.reason.expired", new Object[0], "", LocaleContextHolder.getLocale()));
        } else if ("denied".equals(reason)) {
            mnv.addObject("reason",
                    message.getMessage("login.reason.denied", new Object[0], "", LocaleContextHolder.getLocale()));
        } else {
            mnv.addObject("reason", "");
        }

        return mnv;
    }
}
