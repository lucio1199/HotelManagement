package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Employee;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Employee} entities.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds all employees with the given last name.
     *
     * @param lastName the last name of the employees
     * @return a list of {@link Employee} entities with the specified last name
     */
    List<Employee> findByLastName(String lastName);

    /**
     * Finds all employees with the specified role type.
     *
     * @param roleType the role type to filter by
     * @return a list of {@link Employee} entities with the specified role type
     */
    List<Employee> findByRoleType(RoleType roleType);
}
