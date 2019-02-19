# SpringBootDemoNotes

* [一、属性注入配置简化](#一属性注入配置简化)
* [二、自动配置原理](#二自动配置原理)

## 一、属性注入配置简化

### 1、比较繁杂的JavaConfig配置

> 项目: `springboot-01-config-01`

java配置主要靠java类和一些注解，比较常用的注解有：

- `@Configuration`：声明一个类作为配置类，代替xml文件；
- `@Bean`：声明在方法上，将方法的返回值加入Bean容器，代替`<bean>`标签；
- `@value`：属性注入；
- `@PropertySource`：指定外部属性文件；

我们接下来用java配置来尝试实现连接池配置：

首先引入Druid连接池依赖：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.6</version>
</dependency>
```

创建一个`jdbc.properties`文件，编写jdbc属性：

```properties
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://127.0.0.1:3306/springboot
jdbc.username=root
jdbc.password=root
```
然后编写代码：

```java
@Configuration
@PropertySource("classpath:jdbc.properties")//属性文件位置
public class JdbcConfig {

    //从属性文件 jdbc.properties中注入
    @Value("${jdbc.url}")
    String url;
    @Value("${jdbc.driverClassName}")
    String driverClassName;
    @Value("${jdbc.username}")
    String username;
    @Value("${jdbc.password}")
    String password;

    @Bean // 加入到容器中
    public DataSource dataSource(){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}

```

解读：

- `@Configuration`：声明我们`JdbcConfig`是一个配置类；
- `@PropertySource`：指定属性文件的路径是:`classpath:jdbc.properties`；
- 通过`@Value`为属性注入值；
- 通过@Bean将 `dataSource()`方法声明为一个注册Bean的方法，Spring会自动调用该方法，将方法的返回值加入Spring容器中；

然后我们就可以在任意位置通过`@Autowired`注入DataSource了！

测试：

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBoot01Config01ApplicationTests {


	@Autowired
	private DataSource dataSource;

	@Test
	public void contextLoads() {

		System.out.println(dataSource);
	}

}

```

然后Debug运行并查看：

![](images/sb1_注入成功.png)

属性注入成功了！

### 2、优化注入方式一

在上面的案例中，我们实验了java配置方式。不过属性注入使用的是`@Value`注解。这种方式虽然可行，但是不够强大，**因为它只能注入基本类型值**。

在SpringBoot中，提供了一种新的属性注入方式，支持各种java基本数据类型及复杂类型的注入。

1）我们新建一个类，用来进行属性注入：

```java
@ConfigurationProperties(prefix = "jdbc")// 注意加一个前缀
public class JdbcProperties {
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    // ... 略
    // getters 和 setters
}

```

- 在类上通过`@ConfigurationProperties`注解声明当前类为**属性读取类**；

- `prefix="jdbc"`读取属性文件中，前缀为jdbc的值；

- 在类上定义各个属性，名称必须与属性文件中`jdbc.`后面部分一致；

- 需要注意的是，这里我们并没有指定属性文件的地址，所以我们需要把`jdbc.properties`名称改为`application.properties`，这是SpringBoot默认读取的属性文件名：

   ![pic](images/sb2_properties.png)

2）在JdbcConfig中使用这个属性：

```java
@Configuration
//@PropertySource("classpath:jdbc.properties")  //这里不需要了
@EnableConfigurationProperties(JdbcProperties.class)  // 使用某个配置属性 (这里使用我们的属性JdbcProperties)
public class JdbcConfig {

    @Bean // Spring会调用这个, 因为注解EnableConfigurationProperties会应用那个属性
    public DataSource dataSource(JdbcProperties prop){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(prop.getDriverClassName());
        dataSource.setUrl(prop.getUrl());
        dataSource.setUsername(prop.getUrl());
        dataSource.setPassword(prop.getPassword());
        return dataSource;
    }
}

```

- 通过`@EnableConfigurationProperties(JdbcProperties.class)`来声明要使用`JdbcProperties`这个类的对象；

- 然后你可以通过以下方式注入`JdbcProperties`：

  - @Autowired注入

    ```java
    @Autowired
    private JdbcProperties prop;
    ```

  - 构造函数注入

    ```java
    private JdbcProperties prop;
    public JdbcConfig(Jdbcproperties prop){
        this.prop = prop;
    }
    ```

  - 声明有@Bean的方法参数注入

    ```java
    @Bean
    public Datasource dataSource(JdbcProperties prop){
        // ...
    }
    ```

本例中，我们采用第三种方式。

3）测试结果：

![](images/sb3_注入成功.png)

大家会觉得这种方式似乎更麻烦了，**事实上这种方式有更强大的功能**，**也是SpringBoot推荐的注入方式**。两者对比关系：

![](images/sb4_松散绑定.png)

优势( 即 `Relaxed binding`：松散绑定)

- 不严格要求属性文件中的属性名与成员变量名一致。**支持驼峰，中划线，下划线**等等转换，甚至支持对象引导。比如：`user.friend.name`：代表的是user对象中的friend属性中的name属性，显然friend也是对象。`@value`注解就难以完成这样的注入方式；

- `meta-data suppor`：元数据支持，帮助IDE生成属性提示（写开源框架会用到）；


### 3、优化注入方式二－更优雅的注入

事实上，如果一段属性只有一个`Bean`需要使用，我们无需将其注入到一个类（`JdbcProperties`）中。而是直接在需要的地方声明即可：

```java
@Configuration
public class JdbcConfig {
    @Bean // Spring会调用这个, 因为注解EnableConfigurationProperties会应用那个属性
    // 声明要注入的属性前缀，SpringBoot会自动把相关属性通过set方法注入到DataSource中
    @ConfigurationProperties(prefix = "jdbc")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        return dataSource;
    }
}

```

我们直接把`@ConfigurationProperties(prefix = "jdbc")`声明在需要使用的`@Bean`的方法上，然后SpringBoot就会自动调用这个Bean（此处是DataSource）的`set`方法，然后完成注入。使用的前提是：**该类必须有对应属性的set方法！**

再次测试：

![](images/sb5_注入成功.png)

## 二、自动配置原理

使用SpringBoot之后，一个整合了SpringMVC的WEB工程开发，变的无比简单，那些繁杂的配置都消失不见了，这是如何做到的？

一切魔力的开始，都是从我们的`main()`函数来的，所以我们再次来看下启动类：

```java
@SpringBootApplication
public class SpringBoot01Config03Application {
	public static void main(String[] args) {
		SpringApplication.run(SpringBoot01Config03Application.class, args);
	}
}
```

我们发现特别的地方有两个：

- 注解：`@SpringBootApplication`
- run方法：`SpringApplication.run()`

我们分别来研究这两个部分。

### 1、了解@SpringBootApplication

点击进入，查看源码：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
```



这里重点的注解有3个：

- `@SpringBootConfiguration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

#### 1)、@SpringBootConfiguration

我们继续点击查看源码：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
}
```

通过这段我们可以看出，在这个注解上面，又有一个`@Configuration`注解。

通过上面的注释阅读我们知道：**这个注解的作用就是声明当前类是一个配置类**，然后Spring会自动扫描到添加了`@Configuration`的类，并且读取其中的配置信息。

而`@SpringBootConfiguration`是来声明当前类是SpringBoot应用的配置类，项目中只能有一个。所以一般我们无需自己添加。

#### 2)、@EnableAutoConfiguration

关于这个注解，官网上有一段说明：

> The second class-level annotation is `@EnableAutoConfiguration`. This annotation tells Spring Boot to “guess” how you want to configure Spring, based on the jar dependencies that you have added. Since `spring-boot-starter-web` added Tomcat and Spring MVC, the auto-configuration assumes that you are developing a web application and sets up Spring accordingly.

简单翻译如下：

> 第二级的注解`@EnableAutoConfiguration`，告诉SpringBoot基于你所添加的依赖，去“猜测”你想要如何配置Spring。
>
> 比如我们引入了`spring-boot-starter-web`，而这个启动器中帮我们添加了`tomcat`、`SpringMVC`的依赖。此时自动配置就知道你是要开发一个web应用，所以就帮你完成了web及SpringMVC的默认配置了！

总结，SpringBoot内部对大量的第三方库或Spring内部库进行了**默认配置，这些配置是否生效，取决于我们是否引入了对应库所需的依赖**，如果有那么默认配置就会生效。

所以，我们使用SpringBoot构建一个项目，**只需要引入所需框架的依赖，配置就可以交给SpringBoot处理了**。除非你不希望使用SpringBoot的默认配置，它也提供了自定义配置的入口。

#### 3)、@ComponentScan

我们跟进源码：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Repeatable(ComponentScans.class)
public @interface ComponentScan {
    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};
```

并没有看到什么特殊的地方。我们查看注释：

```java
/**
 * Configures component scanning directives for use with @{@link Configuration} classes.
 * Provides support parallel with Spring XML's {@code <context:component-scan>} element.
 * <p>Either {@link #basePackageClasses} or {@link #basePackages} (or its alias
 * {@link #value}) may be specified to define specific packages to scan. If specific
 * packages are not defined, scanning will occur from the package of the
 * class that declares this annotation.
 *
 **/
```

大概的意思：

* 配置组件扫描的指令。提供了类似与`<context:component-scan>`标签的作用；
* 通过`basePackageClasses`或者`basePackages`属性来指定要扫描的包。**如果没有指定这些属性，那么将从声明这个注解的类所在的包开始**，扫描包及子包；

而我们的`@SpringBootApplication`注解声明的类就是main函数所在的启动类，因此扫描的包是该类所在包及其子包。因此，**一般启动类会放在一个比较前的包目录中。**

### 2、默认配置原理

#### 1)、默认配置类

通过刚才的学习，我们知道`@EnableAutoConfiguration`会开启SpringBoot的自动配置，并且根据你引入的依赖来生效对应的默认配置。那么问题来了：

- 这些默认配置是在哪里定义的呢？
- 为何依赖引入就会触发配置呢？

其实在我们的项目中，已经引入了一个依赖：`spring-boot-autoconfigure`，其中定义了大量自动配置类：

![](images/sb6.png)

几乎涵盖了现在主流的开源框架，例如：

- redis
- jms
- amqp
- jdbc
- jackson
- mongodb
- jpa
- solr
- elasticsearch

... 等等

我们来看一个我们熟悉的，例如SpringMVC，查看mvc 的自动配置类`WebMvcAutoConfiguration`:

```java
@Configuration
@ConditionalOnWebApplication(
    type = Type.SERVLET
)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnMissingBean({WebMvcConfigurationSupport.class})
@AutoConfigureOrder(-2147483638)
@AutoConfigureAfter({DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class, ValidationAutoConfiguration.class})
public class WebMvcAutoConfiguration {
    public static final String DEFAULT_PREFIX = "";
    public static final String DEFAULT_SUFFIX = "";
    private static final String[] SERVLET_LOCATIONS = new String[]{"/"};
    public WebMvcAutoConfiguration() {
    }
}
```

我们看到这个类上的4个注解：

- `@Configuration`：声明这个类是一个配置类


- `@ConditionalOnWebApplication(type = Type.SERVLET)`

  ConditionalOn，翻译就是在某个条件下，此处就是满足项目的类是是Type.SERVLET类型，也就是一个普通web工程，显然我们就是

- `@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })`

  这里的条件是OnClass，也就是满足以下类存在：Servlet、DispatcherServlet、WebMvcConfigurer，其中Servlet只要引入了tomcat依赖自然会有，后两个需要引入SpringMVC才会有。这里就是判断你是否引入了相关依赖，引入依赖后该条件成立，当前类的配置才会生效！

- `@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)`

  这个条件与上面不同，OnMissingBean，是说环境中没有指定的Bean这个才生效。其实这就是自定义配置的入口，**也就是说，如果我们自己配置了一个WebMVCConfigurationSupport的类，那么这个默认配置就会失效！**

接着，我们查看该类中定义了什么：

视图解析器：

![pic](images/sb7.png)

处理器适配器（HandlerAdapter）：

![pic](images/sb8.png)

还有很多，这里就不一一截图了。

#### 2)、默认配置属性

另外，这些默认配置的属性来自哪里呢？

![](images/sb9.png)

我们看到，这里通过`@EnableAutoConfiguration`注解引入了两个属性：`WebMvcProperties`和`ResourceProperties`。这不正是SpringBoot的属性注入玩法嘛。

我们查看这两个属性类：

![](images/sb10.png)

找到了内部资源视图解析器的`prefix`和`suffix`属性。

`ResourceProperties`中主要定义了静态资源（`.js,.html,.css`等)的路径：

```java
@ConfigurationProperties(
    prefix = "spring.resources",
    ignoreUnknownFields = false
)
public class ResourceProperties {
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = new String[]{"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};
    private String[] staticLocations;
    private boolean addMappings;
    private final ResourceProperties.Chain chain;
    private final ResourceProperties.Cache cache;
```

如果我们要覆盖这些默认属性，只需要在`application.properties`中定义与其前缀prefix和字段名一致的属性即可。

### 3、总结

SpringBoot为我们提供了默认配置，而默认配置生效的条件一般有两个：

- **你引入了相关依赖**
- **你自己没有配置**

1）启动器

所以，我们如果不想配置，只需要引入依赖即可，而依赖版本我们也不用操心，因为只要引入了SpringBoot提供的stater（启动器），就会自动管理依赖及版本了。

因此，玩SpringBoot的第一件事情，就是找启动器，SpringBoot提供了大量的默认启动器。

2）全局配置

另外，SpringBoot的默认配置，都会读取默认属性，而这些属性可以通过自定义`application.properties`文件来进行覆盖。这样虽然使用的还是默认配置，但是配置中的值改成了我们自定义的。

因此，玩SpringBoot的第二件事情，就是通过`application.properties`来覆盖默认属性值，形成自定义配置。我们需要知道SpringBoot的默认属性key，非常多。

## 三、SpringBoot实践-简单crud

接下来，我们来看看如何用SpringBoot来玩转以前的SSM,我们沿用之前讲解SSM用到的数据库tb_user和实体类User。

日志级别控制:

```yaml
logging:
  level: com.zxin.springboot: debug  # key - val 结构
```

### 1、整合SpringMVC

虽然默认配置已经可以使用SpringMVC了，不过我们有时候需要进行自定义配置。

#### 1)、修改端口

查看SpringBoot的全局属性可知，端口通过以下方式配置：

```properties 
server.port=80 #映射端口
```

#### 2)、访问静态资源

现在，我们的项目是一个jar工程，那么就没有webapp，我们的静态资源该放哪里呢？

回顾我们上面看的源码，有一个叫做ResourceProperties的类，里面就定义了静态资源的默认查找路径：

```java
@ConfigurationProperties(
    prefix = "spring.resources",
    ignoreUnknownFields = false
)
public class ResourceProperties {
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = new String[]{"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};
```

默认的静态资源路径为：

- classpath:/META-INF/resources/
- classpath:/resources/
- classpath:/static/
- classpath:/public

只要静态资源放在这些目录中任何一个，SpringMVC都会帮我们处理。

我们习惯会把静态资源放在`classpath:/static/`目录下。我们创建目录，并且添加一些静态资源。

#### 3)、添加拦截器

拦截器也是我们经常需要使用的，在SpringBoot中该如何配置呢？

拦截器不是一个普通属性，而是一个类，所以就要用到java配置方式了。在SpringBoot官方文档中有这么一段说明：

> If you want to keep Spring Boot MVC features and you want to add additional [MVC configuration](https://docs.spring.io/spring/docs/5.0.5.RELEASE/spring-framework-reference/web.html#mvc) (interceptors, formatters, view controllers, and other features), you can add your own `@Configuration` class of type `WebMvcConfigurer` but **without** `@EnableWebMvc`. If you wish to provide custom instances of `RequestMappingHandlerMapping`, `RequestMappingHandlerAdapter`, or `ExceptionHandlerExceptionResolver`, you can declare a `WebMvcRegistrationsAdapter` instance to provide such components.
>
> If you want to take complete control of Spring MVC, you can add your own `@Configuration` annotated with `@EnableWebMvc`.

翻译：

> 如果你想要保持Spring Boot 的一些默认MVC特征，同时又想自定义一些MVC配置（包括：拦截器，格式化器, 视图控制器、消息转换器 等等），你应该让一个类实现`WebMvcConfigurer`，并且添加`@Configuration`注解，但是**千万不要**加`@EnableWebMvc`注解。如果你想要自定义`HandlerMapping`、`HandlerAdapter`、`ExceptionResolver`等组件，你可以创建一个`WebMvcRegistrationsAdapter`实例 来提供以上组件。
>
> 如果你想要完全自定义SpringMVC，不保留SpringBoot提供的一切特征，你可以自己定义类并且添加`@Configuration`注解和`@EnableWebMvc`注解

总结：通过实现`WebMvcConfigurer`并添加`@Configuration`注解来实现自定义部分SpringMvc配置。

首先我们定义一个拦截器：

```java
public class LoginInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);//记录日志
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.debug("preHandle method is now running!");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        logger.debug("postHandle method is now running!");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        logger.debug("afterCompletion method is now running!");
    }
}
```

然后，我们定义配置类，注册拦截器：

```java
@Configuration
public class MvcConfig implements WebMvcConfigurer{
    /**
     * 通过@Bean注解，将我们定义的拦截器注册到Spring容器，不过这个好像也不需要写
     * @return
     */
    @Bean
    public LoginInterceptor loginInterceptor(){
        return new LoginInterceptor();
    }

    /**
     * 重写接口中的addInterceptors方法，添加自定义拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 通过registry来注册拦截器，通过addPathPatterns来添加拦截路径
        registry.addInterceptor(this.loginInterceptor()).addPathPatterns("/**");
    }
}
```

接下来运行并查看日志：

你会发现日志中什么都没有，因为我们记录的log级别是debug，默认是显示info以上，我们需要进行配置。

SpringBoot通过`logging.level.*=debug`来配置日志级别，`*`填写包名

```properties
# 设置com.leyou包的日志级别为debug
logging.level.com.leyou=debug
```

再次运行查看：

```verilog
2018-05-05 17:50:01.811 DEBUG 4548 --- [p-nio-80-exec-1] com.leyou.interceptor.LoginInterceptor   : preHandle method is now running!
2018-05-05 17:50:01.854 DEBUG 4548 --- [p-nio-80-exec-1] com.leyou.interceptor.LoginInterceptor   : postHandle method is now running!
2018-05-05 17:50:01.854 DEBUG 4548 --- [p-nio-80-exec-1] com.leyou.interceptor.LoginInterceptor   : afterCompletion method is now running!
```

### 2、整合jdbc和事务

spring中的jdbc连接和事务是配置中的重要一环，在SpringBoot中该如何处理呢？

答案是不需要处理，我们只要找到SpringBoot提供的启动器即可：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

当然，不要忘了数据库驱动，SpringBoot并不知道我们用的什么数据库，这里我们选择MySQL：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

**至于事务，SpringBoot中通过注解来控制**。就是我们熟知的`@Transactional`

```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User queryById(Long id){
        return this.userMapper.selectByPrimaryKey(id);
    }

    @Transactional
    public void deleteById(Long id){
        this.userMapper.deleteByPrimaryKey(id);
    }
}
```

### 3、整合连接池

其实，在刚才引入jdbc启动器的时候，SpringBoot已经自动帮我们引入了一个连接池：

![pic](images/sb11.png)

HikariCP应该是目前速度最快的连接池了，我们看看它与c3p0的对比：

![](images/sb12.png)

因此，我们只需要指定连接池参数即可：

```properties
# 连接四大参数
spring.datasource.url=jdbc:mysql://localhost:3306/springboot
spring.datasource.username=root
spring.datasource.password=root
# 可省略，SpringBoot自动推断
spring.datasource.driverClassName=com.mysql.jdbc.Driver

spring.datasource.hikari.idle-timeout=60000
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.minimum-idle=10
```

对应的相关yml:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/springboot
    username: root
    password: root
```

当然，如果你更喜欢Druid连接池，也可以使用Druid官方提供的启动器：

```xml
<!-- Druid连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.6</version>
</dependency>
```

而连接信息的配置与上面是类似的，只不过在连接池特有属性上，方式略有不同：

```properties
#初始化连接数
spring.datasource.druid.initial-size=1
#最小空闲连接
spring.datasource.druid.min-idle=1
#最大活动连接
spring.datasource.druid.max-active=20
#获取连接时测试是否可用
spring.datasource.druid.test-on-borrow=true
#监控页面启动
spring.datasource.druid.stat-view-servlet.allow=true
```

### 4、整合mybatis

#### 1)、mybatis

SpringBoot官方并没有提供Mybatis的启动器，不过Mybatis[官网](https://github.com/mybatis/spring-boot-starter)自己实现了：

```xml
<!--mybatis -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.3.2</version>
</dependency>
```

配置，基本没有需要配置的：

```properties
# mybatis 别名扫描
mybatis.type-aliases-package=com.heima.pojo
# mapper.xml文件位置,如果没有映射文件，请注释掉
mybatis.mapper-locations=classpath:mappers/*.xml
```

yaml相关配置:

```yaml
logging:
  level:
    com.zxin.springboot: debug  # key - val 结构

spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/springboot
    username: root
    password: root

mybatis:
#  configuration:
#    map-underscore-to-camel-case: true  # 有了通用mapper了就不要这个了
  type-aliases-package: com.zxin.springboot.pojo
#  mapper-locations: classpath:mapper/*.xml  # 这里用通用mapper 不用xml了(单表)
```

需要注意，这里没有配置mapper接口扫描包，因此我们需要给每一个Mapper接口添加`@Mapper`注解，才能被识别。

```java
@Mapper
public interface UserMapper {
}
```

#### 2)、通用mapper

通用Mapper的作者也为自己的插件编写了启动器，我们直接引入即可：

```xml
<!-- 通用mapper -->
<dependency>
    <groupId>tk.mybatis</groupId>
    <artifactId>mapper-spring-boot-starter</artifactId>
    <version>2.0.2</version>
</dependency>
```

不需要做任何配置就可以使用了。

```java
@Mapper
public interface UserMapper extends tk.mybatis.mapper.common.Mapper<User>{//注意是通用mapper下面的
}
```

### 5、启动测试

将controller进行简单改造：

```java
@RestController
@RequestMapping("user")
public class QueryController {

    @Autowired
    private UserService userService;

    @GetMapping("{id}")
    public User hello(@PathVariable("id") Integer id){
        return userService.queryById(id);
    }
}
```

## 四、Thymeleaf快速入门

SpringBoot并不推荐使用jsp，但是支持一些模板引擎技术：

![pic](images/sb13.png)

以前大家用的比较多的是Freemarker，但是我们今天的主角是Thymeleaf！

### 1、为什么是Thymeleaf？

简单说， Thymeleaf 是一个跟 Velocity、FreeMarker 类似的模板引擎，它可以完全替代 JSP 。相较与其他的模板引擎，它有如下三个极吸引人的特点：

- 动静结合：Thymeleaf 在有网络和无网络的环境下皆可运行，即它可以让美工在浏览器查看页面的静态效果，也可以让程序员在服务器查看带数据的动态页面效果。这是由于它支持 html 原型，然后在 html 标签里增加额外的属性来达到模板+数据的展示方式。浏览器解释 html 时会忽略未定义的标签属性，所以 thymeleaf 的模板可以静态地运行；当有数据返回到页面时，Thymeleaf 标签会动态地替换掉静态内容，使页面动态显示。
- 开箱即用：它提供标准和spring标准两种方言，可以直接套用模板实现JSTL、 OGNL表达式效果，避免每天套模板、该jstl、改标签的困扰。同时开发人员也可以扩展和创建自定义的方言。
- 多方言支持：Thymeleaf 提供spring标准方言和一个与 SpringMVC 完美集成的可选模块，可以快速的实现表单绑定、属性编辑器、国际化等功能。
- 与SpringBoot完美整合，SpringBoot提供了Thymeleaf的默认配置，并且为Thymeleaf设置了视图解析器，我们可以像以前操作jsp一样来操作Thymeleaf。代码几乎没有任何区别，就是在模板语法上有区别。

接下来，我们就通过入门案例来体会Thymeleaf的魅力：

### 2、编写接口

编写一个controller，返回一些用户数据，放入模型中，等会在页面渲染

```java
@GetMapping("/all")
public String all(ModelMap model) {
    // 查询用户
    List<User> users = this.userService.queryAll();
    // 放入模型
    model.addAttribute("users", users);
    // 返回模板名称（就是classpath:/templates/目录下的html文件名）
    return "users";
}
```

### 3、引入启动器

直接引入启动器：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

SpringBoot会自动为Thymeleaf注册一个视图解析器：

![pic](images/sb14.png)

与解析JSP的InternalViewResolver类似，Thymeleaf也会根据前缀和后缀来确定模板文件的位置：

![1525522811359](images/sb15.png)

- 默认前缀：`classpath:/templates/`
- 默认后缀：`.html`

所以如果我们返回视图：`users`，会指向到 `classpath:/templates/users.html`

一般我们无需进行修改，默认即可。

### 4、静态页面

根据上面的文档介绍，模板默认放在classpath下的templates文件夹，我们新建一个html文件放入其中：

 ![1525521721279](images/sb16.png)

编写html模板，渲染模型中的数据：

注意，把html 的名称空间，改成：`xmlns:th="http://www.thymeleaf.org"` 会有语法提示

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>首页</title>
    <style type="text/css">
        table {border-collapse: collapse; font-size: 14px; width: 80%; margin: auto}
        table, th, td {border: 1px solid darkslategray;padding: 10px}
    </style>
</head>
<body>
<div style="text-align: center">
    <span style="color: darkslategray; font-size: 30px">欢迎光临！</span>
    <hr/>
    <table class="list">
        <tr>
            <th>id</th>
            <th>姓名</th>
            <th>用户名</th>
            <th>年龄</th>
            <th>性别</th>
            <th>生日</th>
            <th>备注</th>
        </tr>
        <tr th:each="user : ${users}">
            <td th:text="${user.id}">1</td>
            <td th:text="${user.name}">张三</td>
            <td th:text="${user.userName}">zhangsan</td>
            <td th:text="${user.age}">20</td>
            <td th:text="${user.sex} == 1 ? '男': '女'">男</td>
            <td th:text="${#dates.format(user.birthday, 'yyyy-MM-dd')}">1980-02-30</td>
            <td th:text="${user.note}">1</td>
        </tr>
    </table>
</div>
</body>
</html>
```

我们看到这里使用了以下语法：

- `${}` ：这个类似与el表达式，但其实是ognl的语法，比el表达式更加强大
- `th-`指令：`th-`是利用了Html5中的自定义属性来实现的。如果不支持H5，可以用`data-th-`来代替
  - `th:each`：类似于`c:foreach`  遍历集合，但是语法更加简洁
  - `th:text`：声明标签中的文本
    - 例如`<td th-text='${user.id}'>1</td>`，如果user.id有值，会覆盖默认的1
    - 如果没有值，则会显示td中默认的1。这正是thymeleaf能够动静结合的原因，模板解析失败不影响页面的显示效果，因为会显示默认值！

### 5、测试

### 6、模板缓存

Thymeleaf会在第一次对模板解析之后进行缓存，极大的提高了并发处理能力。但是这给我们开发带来了不便，修改页面后并不会立刻看到效果，我们开发阶段可以关掉缓存使用：

```properties
spring.thymeleaf.cache=false # 开发阶段关闭thymeleaf的模板缓存
```

**注意**：

​	在Idea中，我们需要在修改页面后按快捷键：`Ctrl + Shift + F9` 对项目进行rebuild才可以。

​	eclipse中没有测试过。

我们可以修改页面，测试一下。