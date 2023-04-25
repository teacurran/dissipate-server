package app.dissipate.api.grpc;

public interface GrpcRole {
    String ACCESSOR = "grpc";
    static String grpc() {
        return ACCESSOR;
    }
}
