package ma.projet.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ma.projet.dao.IDao;
import ma.projet.entities.Role;
import ma.projet.repository.RoleRepository;

@Service
public class RoleService  implements IDao<Role>{
	@Autowired
	private RoleRepository roleRepository;

	@Override
	public Role create(Role o) {
		return roleRepository.save(o);
	}

	@Override
	public boolean delete(Role o) {
		try {
			roleRepository.delete(o);
			return true;
		}
		catch(Exception ex) {
			return false;
		}
	}

	@Override
	public Role update(Role o) {
		return roleRepository.save(o);
	}

	@Override
	public List<Role> findAll() {
		return roleRepository.findAll();
	}

	@Override
	public Role findById(Long id) {
		return roleRepository.findById(id).orElse(null);
	}
	

}
