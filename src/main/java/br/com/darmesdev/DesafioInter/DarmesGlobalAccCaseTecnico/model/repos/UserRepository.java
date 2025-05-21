package br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.repos;


import br.com.darmesdev.DesafioInter.DarmesGlobalAccCaseTecnico.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserPF u WHERE u.cpf = :cpf")
    boolean existsByCpf(@Param("cpf") String cpf);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserPJ u WHERE u.cnpj = :cnpj")
    boolean existsByCnpj(@Param("cnpj") String cnpj);
}
