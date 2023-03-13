## Usage
```text
 ___________        __          ______    
("     _   ")      /""\        /    " \   
 )__/  \\__/      /    \      // ____  \  
    \\_ /        /' /\  \    /  /    ) :) 
    |.  |       //  __'  \  (: (____/ //  
    \:  |      /   /  \\  \  \        /   
     \__|     (___/    \___)  \"_____/    
                                          

usage: java -jar Tao.jar [-cp <arg>] [-h] [-lp <arg>] [-o <arg>] [-sp
       <arg>]
 -cp,--class-path <arg>   类文件地址
 -h,--help                打印命令行帮助信息
 -lp,--lib-path <arg>     库文件地址
 -o,--outPut <arg>        结果保存目录
 -sp,--sinks-path <arg>   设置sink文件地址
```

# 结果样例
Graphviz Dot格式
```text
digraph CallGraph{
    node [fontsize="20",];
    edge [fontsize="10",];
    0[label="<com.ruoyi.generator.controller.GenController$lambda_dataList_3__9: java.lang.String operation(int,int)>", shape="box"];
    1[label="<com.ruoyi.generator.controller.GenController: java.lang.String operate(int,int,com.ruoyi.generator.controller.GenController$MathOperation)>", shape="box"];
    2[label="<com.alibaba.fastjson.JSONObject: com.alibaba.fastjson.JSONObject parseObject(java.lang.String)>", shape="box"];
    3[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: void setTableFromOptions(com.ruoyi.generator.domain.GenTable)>", shape="box"];
    4[label="<com.ruoyi.generator.util.VelocityUtils: void setTreeVelocityContext(org.apache.velocity.VelocityContext,com.ruoyi.generator.domain.GenTable)>", shape="box"];
    5[label="<com.ruoyi.generator.controller.GenController: java.lang.String lambda$dataList$3(int,int)>", shape="box"];
    6[label="<com.ruoyi.common.utils.AddressUtils: java.lang.String getRealAddressByIP(java.lang.String)>", shape="box"];
    7[label="<com.ruoyi.generator.util.VelocityUtils: int getExpandColumn(com.ruoyi.generator.domain.GenTable)>", shape="box"];
    8[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: byte[] downloadCode(java.lang.String)>", shape="box"];
    9[label="<com.ruoyi.generator.controller.GenController: com.ruoyi.common.core.domain.AjaxResult preview(java.lang.Long)>", shape="box"];
    10[label="<com.ruoyi.framework.manager.factory.AsyncFactory$2: void run()>", shape="box"];
    11[label="<com.ruoyi.generator.util.VelocityUtils: void setMenuVelocityContext(org.apache.velocity.VelocityContext,com.ruoyi.generator.domain.GenTable)>", shape="box"];
    12[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: java.util.Map previewCode(java.lang.Long)>", shape="box"];
    13[label="<com.ruoyi.framework.manager.factory.AsyncFactory$3: void run()>", shape="box"];
    14[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: void validateEdit(com.ruoyi.generator.domain.GenTable)>", shape="box"];
    15[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: void generatorCode(java.lang.String)>", shape="box"];
    16[label="<com.ruoyi.generator.controller.GenController: void download(javax.servlet.http.HttpServletResponse,java.lang.String)>", shape="box"];
    17[label="<com.ruoyi.generator.controller.GenController: void batchGenCode(javax.servlet.http.HttpServletResponse,java.lang.String)>", shape="box"];
    18[label="<com.ruoyi.generator.controller.GenController: com.ruoyi.common.core.domain.AjaxResult editSave(com.ruoyi.generator.domain.GenTable)>", shape="box"];
    19[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: void generatorCode(java.lang.String,java.util.zip.ZipOutputStream)>", shape="box"];
    20[label="<com.ruoyi.generator.service.impl.GenTableServiceImpl: byte[] downloadCode(java.lang.String[])>", shape="box"];
    21[label="<com.ruoyi.generator.controller.GenController: com.ruoyi.common.core.page.TableDataInfo dataList(com.ruoyi.generator.domain.GenTable)>", shape="box"];
    22[label="<com.ruoyi.generator.controller.GenController: com.ruoyi.common.core.domain.AjaxResult genCode(java.lang.String)>", shape="box"];
    23[label="<com.ruoyi.framework.manager.factory.AsyncFactory$1: void run()>", shape="box"];
    24[label="<com.ruoyi.generator.util.VelocityUtils: org.apache.velocity.VelocityContext prepareContext(com.ruoyi.generator.domain.GenTable)>", shape="box"];
    21 -> 1 [label="Special (109)"];
    24 -> 11 [label="Static (36)"];
    5 -> 2 [label="Static (109)"];
    18 -> 14 [label="Interface (210)"];
    16 -> 8 [label="Interface (280)"];
    6 -> 2 [label="Static (28)"];
    17 -> 20 [label="Interface (319)"];
    24 -> 4 [label="Static (36)"];
    11 -> 2 [label="Static (73)"];
    21 -> 0 [label="Static (109)"];
    23 -> 6 [label="Static (45)"];
    8 -> 19 [label="Special (243)"];
    3 -> 2 [label="Static (501)"];
    14 -> 2 [label="Static (414)"];
    20 -> 19 [label="Special (356)"];
    12 -> 24 [label="Static (210)"];
    19 -> 24 [label="Static (372)"];
    4 -> 2 [label="Static (81)"];
    1 -> 0 [label="Interface (345)"];
    9 -> 12 [label="Interface (268)"];
    4 -> 7 [label="Static (81)"];
    15 -> 24 [label="Static (259)"];
    13 -> 6 [label="Static (101)"];
    7 -> 2 [label="Static (366)"];
    0 -> 5 [label="Static (-1)"];
    22 -> 15 [label="Interface (293)"];
    10 -> 6 [label="Static (77)"];
}
```

# 调用类型
1. Static
2. Special
3. Virtual
4. Interface
5. 特殊：lambda

# 核心算法
[Class Hierarchy Analysis（CHA）](https://link.springer.com/chapter/10.1007/3-540-49538-X_5)


