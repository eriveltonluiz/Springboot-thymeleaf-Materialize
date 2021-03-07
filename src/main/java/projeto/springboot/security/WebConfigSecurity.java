package projeto.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private ImplementacaoUserDetailService implementacaoUserDetailService;
	
	@Override // Configura as solicitações de acesso por http
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()  //Desativa as configurações padrão de memória do spring.
		.authorizeRequests()   // Permitir restringir acessos
		.antMatchers(HttpMethod.GET, "/").permitAll()   //Qualquer usuário acessa a página inicial
		.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN")  //não é necessãrio colocar ROLE_
		.anyRequest().authenticated()
		.and().formLogin().permitAll()   //permite qualquer usuário
		.loginPage("/login")
		.defaultSuccessUrl("/cadastropessoa")
		.failureUrl("/login?error=true")
		.and().logout().logoutSuccessUrl("/login")   //Mapeia URL de logout e invalida usuário autenticado
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
	@Override  //Cria autenticação do usuário com BD ou em memória
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(implementacaoUserDetailService)
		.passwordEncoder(new BCryptPasswordEncoder());
		
		/*auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
		.withUser("Erivelton")
		.password("123")
		.roles("ADMIN");*/
	}
	
	@Override   //Ignora URL específicas
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/materialize/**");
	}
	
}
