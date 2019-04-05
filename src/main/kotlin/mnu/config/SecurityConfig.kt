package mnu.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.context.request.RequestContextListener
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private var dataSource: DataSource? = null

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun requestContextListener(): RequestContextListener {
        return RequestContextListener()
    }


    override fun configure(web: WebSecurity) {
        web
            .ignoring()
            .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/pics/**")
    }

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
                .authorizeRequests()
            // TODO requests for every authority (admin, client etc.)
                .antMatchers( "/auth/login", "/auth/register", "/", "/*.ico", "/img/**", "/*.js", "/index.html").permitAll()
                .anyRequest().authenticated()
            .and()
                .formLogin()
                .loginPage("/auth/login")
                .successHandler { request, response, authentication -> run {
                    val roles = AuthorityUtils.authorityListToSet(authentication.authorities)
                    response.status = 200
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.outputStream.print("{\"Login\":\"" + request.getParameter("login") + "\"}")
                    if (roles.contains("ADMIN"))
                        response.sendRedirect("/admin/main")
                    if (roles.contains("MANAGER"))
                        response.sendRedirect("/managersMenu")
                    if (roles.contains("SCIENTIST"))
                        response.sendRedirect("/sci/main")
                    if (roles.contains("SECURITY"))
                        response.sendRedirect("/secMain")
                    if (roles.contains("CUSTOMER") || roles.contains("MANUFACTURER"))
                        response.sendRedirect("/clientsShop")
                    if (roles.contains("PRAWN"))
                        response.sendRedirect("/prawnMain")
                }}
                .failureHandler { request, response, exception -> response.status = 401 }
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/")
            .and()
                .exceptionHandling().authenticationEntryPoint { request, response, authException -> run {
                response.outputStream.print("Not authorized")
                response.status = 401
            }}
    }


    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.jdbcAuthentication()
            .dataSource(dataSource)
            .passwordEncoder(passwordEncoder())
            .usersByUsernameQuery("select login, password, 1 from users where login=?")
            .authoritiesByUsernameQuery("select u.login, u.role from users u where u.login=?")
    }


}