package com.formula.formula.restctl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private Logger log = LoggerFactory.getLogger("com.hll.demo");

    @RequestMapping(value = "/hello", method = RequestMethod.POST)
    public TestBean hello(@RequestBody JSONObject res) {

        log.info(res.toJSONString());
        TestBean tb = res.toJavaObject(TestBean.class);

        log.error(tb.toString());

        tb.setName("dddd");
        return tb;
    }


    @RequestMapping(value = "/hello3", method = RequestMethod.GET)
    public String notParam() {

        log.info("");

        TestBean rtn = new TestBean();
        rtn.setName("ddddd");
        return "";
    }


    @RequestMapping(value = "/hello2", method = RequestMethod.GET)
    public String hello2() {


        return "hellow";
    }


    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public TestBean getUser() {
        TestBean rtn = new TestBean();
        rtn.setName("Tome1");
        rtn.setId("00001");
        rtn.setMark("sdfasdfsadfasdfasdfaasdfasdf");
        return rtn;
    }

    @RequestMapping(value = "/getUserById", method = RequestMethod.GET)
    public TestBean getUseddrById(@RequestBody JSONObject res) {

        log.info(res.toJSONString());
        String id = res.getString("userid");

        TestBean rtn = new TestBean();
        rtn.setName("Tome");
        rtn.setId(id);
        rtn.setMark("sdfasdfsadfasdfasdfaaeesdfasdf");
        return rtn;
    }


}

class TestBean {

    private String name;

    private String id;
    @JSONField(serialize = false)
    private String mark;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public String getId() {
        return id;
    }

    public String getMark() {
        return mark;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "my name is " + this.name;
    }
}
