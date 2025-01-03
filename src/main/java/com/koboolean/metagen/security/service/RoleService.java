package com.koboolean.metagen.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final com.koboolean.metagen.security.repository.RoleHierarchyRepository roleHierarchyRepository;


    @Transactional
    public String findAllHierarchy() {

        List<com.koboolean.metagen.domain.entity.RoleHierarchy> hierarchyDtos = roleHierarchyRepository.findAll();


        StringBuilder hierarchyRole = new StringBuilder();

        if(!hierarchyDtos.isEmpty()){

            Iterator<com.koboolean.metagen.domain.entity.RoleHierarchy> itr = hierarchyDtos.iterator();

            while (itr.hasNext()) {
                com.koboolean.metagen.domain.entity.RoleHierarchy roleHierarchy = itr.next();
                if (roleHierarchy.getParent() != null) {
                    hierarchyRole.append(roleHierarchy.getParent().getRoleName());
                    hierarchyRole.append(" > ");
                    hierarchyRole.append(roleHierarchy.getRoleName());
                    hierarchyRole.append("\n");
                }
            }
        }



        return hierarchyRole.toString();
    }

}
