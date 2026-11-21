package br.unitins.tp2.testes;

// Usuarios do import.sql usados nos testes com @QuarkusTest. O "sub" e o keycloakId do seed: com ele o
// UsuarioLogadoService e o ContaAtivaAugmentor encontram o perfil local, como num login de verdade.
// Uso: @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
//      @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
public final class Usuarios {

    public static final String ADMIN = "admin@fincontrol.com";
    public static final String ADMIN_SUB = "0f1c2a3b-0000-4000-8000-000000000001";

    // Joao: USER no plano Empreendedor (id 2)
    public static final String JOAO = "joao@email.com";
    public static final String JOAO_SUB = "0f1c2a3b-0000-4000-8000-000000000002";

    private Usuarios() {
    }
}
