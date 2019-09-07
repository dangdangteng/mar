```
    项目结构:
        src.main
            -java
                -com.dinglicom.mr
                    -config
                        配置类.class
                    -constants
                        固定参数
                    -controller
                        -job
                            worker工程回调接口.class
                        -view
                            视图控制类.class
                    -entity
                        -correlationdata
                            压入队列中的correlation数据实体类.class
                        -page
                            分页实体类.class
                        数据库对应实体类.class
                    -Enum
                        枚举类.class
                    -exception
                        异常处理类.class
                    -producer
                        -confirm
                            队列回调类.class
                        队列操作类.class
                    -repository
                        jpa.class
                    -response
                        返回类.class
                    -service
                        逻辑处理.class
                    -task
                        定时任务.class
                    -util
                        工具类.class
                    application.class
            -resources
                配置文件
```

## 项目技术要点
> springboot + springcloud + 配合eureka 实现远程服务调用，传输数据压缩，响应时间配置
> DataSource-> MySQL : hikari 高可用数据库连接池，配置很完美不需要更改，jpa 实现数据响应
> rabbit mq 
> quratz