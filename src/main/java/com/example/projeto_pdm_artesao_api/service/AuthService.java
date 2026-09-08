package com.example.projeto_pdm_artesao_api.service;

import com.example.projeto_pdm_artesao_api.dto.ArtesaoCreateDTO;
import com.example.projeto_pdm_artesao_api.dto.ArtesaoResponse;
import com.example.projeto_pdm_artesao_api.dto.AuthResponse;
import com.example.projeto_pdm_artesao_api.dto.LoginRequest;
import com.example.projeto_pdm_artesao_api.entities.Artesao;
import com.example.projeto_pdm_artesao_api.repositories.ArtesaoRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final ArtesaoRepository artesaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService( // constructor DI
        ArtesaoRepository artesaoRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.artesaoRepository = artesaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ArtesaoResponse cadastrar(ArtesaoCreateDTO dto) {

        if (artesaoRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        Artesao artesao = new Artesao();

        artesao.setNome(dto.nome());
        artesao.setTelefone(dto.telefone());
        artesao.setEmail(dto.email());

        String senhaHash = passwordEncoder.encode(dto.senha());

        artesao.setSenha(senhaHash);

        Artesao salvo = artesaoRepository.save(artesao);

        return new ArtesaoResponse(
            salvo.getId(),
            salvo.getNome(),
            salvo.getTelefone(),
            salvo.getEmail()
        );
    }

    public AuthResponse login(LoginRequest dto) {

        Artesao artesao = artesaoRepository
                .findByEmail(dto.email())
                .orElseThrow(()-> 
                new RuntimeException(
                    "E-mail ou senha inválidos"
                )
            );

        boolean senhaValida = passwordEncoder.matches(
            dto.senha(), artesao.getSenha()
        );

        if (!senhaValida) {
            throw new RuntimeException(
                "E-mail ou senha inválidos"
            );
        }

        String token = jwtService.generateToken(artesao);

        return new AuthResponse(
            token,
            artesao.getId(),
            artesao.getNome()
        );
    }
}