package com.dzx.controller;

import com.dzx.bean.Department;
import com.dzx.bean.Msg;
import com.dzx.service.DepartmentService;
import org.apache.taglibs.standard.lang.jstl.NullLiteral;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**处理和部门有关的请求
 * @author DuZengXin
 * @date 2019/8/10 - 9:31
 */
@Controller
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 返回所有的部门信     */
    @RequestMapping("/depts")
    @ResponseBody
    public Msg getDepts(){
        //查出的所有部门信息
        List<Department> list=departmentService.getDepts();

        return Msg.success().add("depts",list);
    }

}
