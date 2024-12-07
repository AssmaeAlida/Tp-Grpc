package ma.projet.grpc.service;

import ma.projet.grpc.entities.Compte;
import ma.projet.grpc.repositories.CompteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompteService {
    private final CompteRepository compteRepository;

    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }
    public List<Compte>  findAllComptes() {
        return compteRepository.findAll();
    }
    public Compte findCompteById(Long id) {
        return compteRepository.findById(id).orElse(null);
    }
    public Compte saveCompte(Compte compte) {
        return compteRepository.save(compte);
    }

    public List<Compte> findComptesByType(String type) {
        return compteRepository.findByType(type); // Assurez-vous que la méthode existe dans le repository
    }

    public boolean deleteCompte(Long id) {
        if (compteRepository.existsById(id)) {
            compteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
