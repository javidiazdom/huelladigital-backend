package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsESALMemberRequestDto;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.domain.service.ESALContactPersonService;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.entities.Credential;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.huellapositiva.util.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class ESALContactPersonServiceShould {
    @Autowired
    private TestData testData;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ESALContactPersonService ESALContactPersonService;

    @Autowired
    private JpaContactPersonRepository organizationMemberRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_a_new_member() {
        // GIVEN
        CredentialsESALMemberRequestDto dto = CredentialsESALMemberRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

        // WHEN
        Id contactPersonId = ESALContactPersonService.registerMember(PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""));

        // THEN
        Optional<JpaContactPerson> employeeOptional = organizationMemberRepository.findByIdWithCredentialsAndRoles(contactPersonId.toString());
        assertTrue(employeeOptional.isPresent());
        JpaContactPerson contactPerson = employeeOptional.get();
        Credential credential = contactPerson.getCredential();
        assertThat(credential.getEmail(), is(DEFAULT_EMAIL));
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, credential.getHashedPassword()), is(true));
        assertThat(credential.getRoles(), hasSize(1));
        assertThat(credential.getRoles().iterator().next().getName(), is(Roles.CONTACT_PERSON_NOT_CONFIRMED.toString()));
    }
}