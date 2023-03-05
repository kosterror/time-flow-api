package ru.hits.timeflowapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.hits.timeflowapi.security.UserDetailsServiceImpl;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final JWTFilter jwtFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/v1/student-requests/**").hasRole("ADMIN")
                .antMatchers("/api/v1/employee-requests/**").hasRole("ADMIN")
                .antMatchers("/api/v1/schedule-maker-requests/**").hasRole("ADMIN")
                .antMatchers("/api/v1/available-timeslots").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .antMatchers("/api/v1/available-teachers").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .antMatchers("/api/v1/available-classrooms").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .antMatchers("/api/v1/account/**").authenticated()
                .antMatchers("/api/v1/sign-out").authenticated()
                .antMatchers("/api/v1/users").hasRole("ADMIN")
                .antMatchers("/api/v1/students").hasRole("ADMIN")
                .antMatchers("/api/v1/employees").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/v1/lessons").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .antMatchers(HttpMethod.POST, "/api/v1/lessons/for-a-few-weeks").hasAnyRole("ADMIN",
                        "SCHEDULE_MAKER")
                .antMatchers(HttpMethod.PUT, "/api/v1/lessons/**").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .antMatchers(HttpMethod.DELETE, "/api/v1/lessons").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .antMatchers(HttpMethod.DELETE, "/api/v1/lessons/**").hasAnyRole("ADMIN", "SCHEDULE_MAKER")
                .anyRequest().permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors()
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsServiceImpl)
                .passwordEncoder(getPasswordEncoder());
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
