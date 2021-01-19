package projeto.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import projeto.springboot.model.Usuario;
import projeto.springboot.repository.UsuarioRepository;

@Service
public class ImplementacaoUserDetailService implements UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@SuppressWarnings("unused")
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findUserByLogin(username);
		usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));

		if (usuario == null) {
			throw new UsernameNotFoundException("Usuário não foi encontrado"); // Mostrar no log para identificação do erro
		}

		return new User(usuario.getLogin(), usuario.getPassword(), usuario.isEnabled(), true, true,
				true, usuario.getAuthorities());
	}

}
