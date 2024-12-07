package ma.projet.grpc.controllers;

import io.grpc.stub.StreamObserver;
import ma.projet.grpc.service.CompteService;
import ma.projet.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final CompteService compteService;

    public CompteServiceImpl(CompteService compteService) {
        this.compteService = compteService;
    }

    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        try {
            List<Compte> comptesList = compteService.findAllComptes().stream()
                    .map(compte -> Compte.newBuilder()
                            .setId(String.valueOf(compte.getId()))
                            .setSolde(compte.getSolde())
                            .setDateCreation(compte.getDateCreation())
                            .setType(TypeCompte.valueOf(compte.getType()))
                            .build())
                    .collect(Collectors.toList());

            responseObserver.onNext(GetAllComptesResponse.newBuilder().addAllComptes(comptesList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Erreur interne : " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        try {
            Long id = Long.valueOf(request.getId());
            var compte = compteService.findCompteById(id);

            if (compte == null) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Compte non trouvé").asRuntimeException());
                return;
            }

            var grpcCompte = Compte.newBuilder()
                    .setId(String.valueOf(compte.getId()))
                    .setSolde(compte.getSolde())
                    .setDateCreation(compte.getDateCreation())
                    .setType(TypeCompte.valueOf(compte.getType()))
                    .build();

            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(grpcCompte).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Erreur interne : " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void compteByType(GetCompteByTypeRequest request, StreamObserver<GetCompteByTypeResponse> responseObserver) {
        try {
            String type = request.getType().name();
            List<ma.projet.grpc.entities.Compte> comptesList = compteService.findComptesByType(type);

            List<ma.projet.grpc.stubs.Compte> grpcComptesList = comptesList.stream().map(compte -> {
                try {
                    // Ensure the type value is valid
                    TypeCompte typeEnum = TypeCompte.valueOf(compte.getType().toUpperCase());
                    return ma.projet.grpc.stubs.Compte.newBuilder()
                            .setId(String.valueOf(compte.getId()))
                            .setSolde(compte.getSolde())
                            .setDateCreation(compte.getDateCreation())
                            .setType(typeEnum)
                            .build();
                } catch (IllegalArgumentException e) {
                    // Handle the error if the type is not valid
                    return null; // Or handle it in another way, such as logging the error
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            responseObserver.onNext(GetCompteByTypeResponse.newBuilder().addAllComptes(grpcComptesList).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Erreur interne : " + e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void deleteCompte(DeleteCompteRequest request, StreamObserver<DeleteCompteResponse> responseObserver) {
        try {
            Long id = request.getId();
            boolean deleted = compteService.deleteCompte(id);

            if (deleted) {
                responseObserver.onNext(DeleteCompteResponse.newBuilder().setSuccess(true).build());
            } else {
                responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Compte non trouvé").asRuntimeException());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Erreur interne : " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void saveCompte(SaveCompteRequest request, StreamObserver<SaveCompteResponse> responseObserver) {
        try {
            var compteReq = request.getCompte();
            if (compteReq.getType() == null) {
                throw new IllegalArgumentException("Type de compte invalide.");
            }
            var compte = new ma.projet.grpc.entities.Compte();
            compte.setSolde(compteReq.getSolde());
            compte.setDateCreation(compteReq.getDateCreation());
            compte.setType(compteReq.getType().name());
            var savedCompte = compteService.saveCompte(compte);

            var grpcCompte = Compte.newBuilder()
                    .setId(String.valueOf(savedCompte.getId()))
                    .setSolde(savedCompte.getSolde())
                    .setDateCreation(savedCompte.getDateCreation())
                    .setType(TypeCompte.valueOf(savedCompte.getType()))
                    .build();

            responseObserver.onNext(SaveCompteResponse.newBuilder().setCompte(grpcCompte).build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Erreur interne : " + e.getMessage()).asRuntimeException());
        }
    }
}
