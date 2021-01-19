package projeto.springboot.controler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import projeto.springboot.model.Pessoa;
import projeto.springboot.model.Telefone;
import projeto.springboot.repository.PessoaRepository;
import projeto.springboot.repository.ProfissaoRepository;
import projeto.springboot.repository.TelefoneRepository;

@Controller
public class PessoaControler {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ProfissaoRepository profissaoRepository;

	// value igual a valor mapeado na url
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView Inicio() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll());
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}

	// ** ignora tudo o que tem antes de salvar na url. BindingResult é o objeto que
	// irá retornar as mensagens e que contém o resultado da validação
	@RequestMapping(method = RequestMethod.POST, consumes = { "multipart/form-data" }, value = "**/salvar")
	public ModelAndView salvar(@Valid @ModelAttribute(value = "pessoa") Pessoa pessoa, BindingResult bindingResult,
			final MultipartFile file) throws IOException {

		if (bindingResult.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
			Iterable<Pessoa> pessoaIt = pessoaRepository.findAll();
			andView.addObject("pessoas", pessoaIt);
			andView.addObject("pessoaobj", pessoa);

			List<String> msg = new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); // vem das anotações @NotEmpty e outras
			}

			andView.addObject("msg", msg);
			return andView;
		}
		
		if(file.getSize() > 0) {
			pessoa.setCurriculo(file.getBytes());
		} else {
			if(pessoa.getId() != null && pessoa.getId() > 0) {
				byte[] curriculoTemp = pessoaRepository.findById(pessoa.getId()).get().getCurriculo();
				pessoa.setCurriculo(curriculoTemp);
			}
		}
		
		pessoaRepository.save(pessoa);
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		Iterable<Pessoa> pessoaIt = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoaIt);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		Iterable<Pessoa> pessoa = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoa);
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}

	// @GetMapping("/editarpessoa/{idpessoa}") é a mesma coisa de
	// @RequestMapping(method = requestMethod.Get, value = ...
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		andView.addObject("pessoaobj", pessoa.get());
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}

	@GetMapping("/excluirpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {
		ModelAndView view = new ModelAndView("cadastro/cadastropessoa");
		pessoaRepository.deleteById(idpessoa);
		view.addObject("pessoaobj", new Pessoa());
		view.addObject("pessoas", pessoaRepository.findAll());
		return view;
	}

	@PostMapping("**/pesquisapessoa")
	public ModelAndView pesquisarPessoa(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("pesqsexo") String pesqsexo) {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		if (pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesqsexo);
		} else {
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
		}

		ModelAndView view = new ModelAndView("cadastro/cadastropessoa");
		view.addObject("pessoas", pessoas);
		view.addObject("pessoaobj", new Pessoa());
		return view;
	}

	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {
		ModelAndView view = new ModelAndView("cadastro/telefones");
		view.addObject("pessoaobj", pessoaRepository.findById(idpessoa).get());
		view.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return view;
	}

	@PostMapping("**/addFonePessoa/{idpessoa}")
	public ModelAndView addFonePessoa(@Valid Telefone telefone, @PathVariable("idpessoa") Long pessoaid,
			BindingResult bindingResult) {

		Optional<Pessoa> p = pessoaRepository.findById(pessoaid);

		if (telefone != null && (telefone.getNumero() != null && telefone.getNumero().isEmpty())
				|| telefone.getTipo().isEmpty()) {
			ModelAndView view = new ModelAndView("cadastro/telefones");
			view.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
			view.addObject("pessoaobj", p.get());

			List<String> msg = new ArrayList<String>();
			if (telefone.getNumero().isEmpty()) {
				msg.add("Número deve ser informado");
			}

			if (telefone.getNumero().isEmpty()) {
				msg.add("Tipo deve ser informado");
			}
			view.addObject("msg", msg);

			return view;
		}

		telefone.setPessoa(p.get());
		telefoneRepository.save(telefone);
		ModelAndView view = new ModelAndView("cadastro/telefones");
		view.addObject("telefones", telefoneRepository.getTelefones(pessoaid));
		view.addObject("pessoaobj", p.get());
		return view;
	}

	@GetMapping("/excluirtelefone/{idtelefone}")
	public ModelAndView excluirtelefone(@PathVariable("idtelefone") Long idtelefone) {

		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		telefoneRepository.deleteById(idtelefone);

		ModelAndView view = new ModelAndView("cadastro/telefones");
		view.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		view.addObject("pessoaobj", pessoa);
		return view;
	}
}
