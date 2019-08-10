package com.dzx.controller;

import com.dzx.bean.Employee;
import com.dzx.bean.Msg;
import com.dzx.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**处理员工CRUD请求
 * @author DuZengXin
 * @date 2019/8/8 - 19:10
 */

@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;


    /**
     * 单个批量二合一
     * 批量:1-2-3
     * 单个:1
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "/emp/{ids}",method = RequestMethod.DELETE)
    @ResponseBody
    public Msg deleteEmp(@PathVariable("ids")String ids){
        //批量删除
        if(ids.contains("-")){
            List<Integer> del_ids=new ArrayList<>();
            String[] str_ids = ids.split("-");
            //组装id的集合
            for (String  str: str_ids) {
                int i = Integer.parseInt(str);
                del_ids.add(i);
            }
            employeeService.deleteBatch(del_ids);
        }else{
            int id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return Msg.success();
    }

    /**

     *
     * 如果直接发送ajax=PUT形式的请求
     * 封装的数据
     * 问题：
     * 请求体中有数据
     * 但是employee对象封装不上
     * 原因：
     * 1.将请求体中的数据，封装一个Map
     * 2.request.getParameter("empName")就会从map中取值。
     * 3.SpringMVC封装POJO对象时，会把POJO每个属性值，调用request.getParameter();来拿
        Ajax发送put请求的问题：
     put请求：请求体中的数据，request.getParameter();都拿不到
            原因：tomcat一看是put就不会封装请求体为map，只有post形式的请求才封装请求体为map

     解决方案
        我们要能直接发送PUT之类的请求还要封装请求体中的数据
     配置上HttpPutFormContentFilter  作用：将请求体中的数据解析包装成一个map。request被重新包装，
     request.getParameter();就会重写，就会从自己封装的map中取数据


     * 员工更新方法
     * @param employee
     * @return
     */
    @RequestMapping(value = "/emp/{empId}",method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee){
        employeeService.updateEmp(employee);
        return Msg.success();

    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @RequestMapping(value = "/emp/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id){
        Employee employee=employeeService.getEmp(id);
        return Msg.success().add("emp",employee);
    }

    /**
     * 检验用户名是否可用
     * @param empName
     * @return
     */
    @RequestMapping("/checkuser")
    @ResponseBody
    public Msg checkuser(@RequestParam("empName") String empName){
        //先判断用户名是否为合法的表达式
        String regx="^([a-zA-Z0-9_-]{3,16}$)|(^[\\u2E80-\\u9FFF]{2,5})";
        if(!empName.matches(regx)){
            return Msg.fail().add("va_msg","用户名必须是3-16位数字和字母的组合或者2-5位中文");
        }
        //数据库用户名重复校验
        boolean b= employeeService.checkUser(empName);
        if(b){
            return Msg.success();
        }else{
            return Msg.fail().add("va_msg","用户名不可用");
        }
    }

    /**
     * 员工添加
     * 1.支持JSR303校验
     * 2.导入Hibernate-Validator
     * @return
     */
    @RequestMapping(value = "/emp",method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result){
        if(result.hasErrors()){
            //校验失败，返回失败，在模态框中显示校验失败的信息
            Map<String,Object> map=new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError fieldError: errors ) {
                System.out.println("错误的字段名："+fieldError.getField());
                System.out.println("错误的信息："+fieldError.getDefaultMessage());
                map.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields",map);
        }else{
        employeeService.saveEmp(employee);
        return Msg.success();
        }
    }

    /**
     * 导入jackson包
     * @param pn
     * @return
     */
    @RequestMapping("/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pn",defaultValue = "1")Integer pn){
//这不是一个分页查询：
        //引入PageHelper分页插件
        //在查询之前只需要调用,传入页码以及每页的大小
        PageHelper.startPage(pn,5);
        //startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps= employeeService.getAll();

        //使用pageInfo包装查询的结果，只需要将paginfo交给页面就行了。
        //封装了详细的分页信息，包括有我们查询出来的数据.传入连续显示的页数
        PageInfo page=new PageInfo(emps,5);
        return Msg.success().add("pageInfo",page);
    }



    /**
     * 查询员工数据（分页查询）
     * @return
     */
    //@RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn",defaultValue = "1")Integer pn,Model model){
        //这不是一个分页查询：
        //引入PageHelper分页插件
        //在查询之前只需要调用,传入页码以及每页的大小
        PageHelper.startPage(pn,5);
        //startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps= employeeService.getAll();

        //使用pageInfo包装查询的结果，只需要将paginfo交给页面就行了。
        //封装了详细的分页信息，包括有我们查询出来的数据.传入连续显示的页数
        PageInfo page=new PageInfo(emps,5);
        model.addAttribute("pageInfo",page);


        return "list";
    }
}
