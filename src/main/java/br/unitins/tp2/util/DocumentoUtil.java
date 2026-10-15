package br.unitins.tp2.util;

// Valida os digitos verificadores de CPF e CNPJ (regra oficial da Receita, modulo 11)
public final class DocumentoUtil {

    private DocumentoUtil() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public static boolean isValido(String documento) {
        if (documento == null)
            return false;
        if (documento.length() == 11)
            return isCpfValido(documento);
        if (documento.length() == 14)
            return isCnpjValido(documento);
        return false;
    }

    public static boolean isCpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || todosIguais(cpf))
            return false;

        int d1 = digitoCpf(cpf, 9);
        int d2 = digitoCpf(cpf, 10);
        return d1 == cpf.charAt(9) - '0' && d2 == cpf.charAt(10) - '0';
    }

    public static boolean isCnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}") || todosIguais(cnpj))
            return false;

        int[] pesos1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int[] pesos2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

        int d1 = digitoCnpj(cnpj, pesos1);
        int d2 = digitoCnpj(cnpj, pesos2);
        return d1 == cnpj.charAt(12) - '0' && d2 == cnpj.charAt(13) - '0';
    }

    // tamanho = quantos digitos entram na conta (9 para o 1o DV, 10 para o 2o)
    private static int digitoCpf(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++)
            soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++)
            soma += (cnpj.charAt(i) - '0') * pesos[i];

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    // 111.111.111-11 passa no calculo, mas nao e um documento real
    private static boolean todosIguais(String valor) {
        return valor.chars().distinct().count() == 1;
    }
}
