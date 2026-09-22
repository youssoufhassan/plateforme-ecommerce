package com.parfum.ecommerce.supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Traduit les erreurs techniques d'un appel HTTP en erreurs fournisseur compréhensibles.
 * Le détail technique est écrit dans les logs, le message renvoyé reste lisible pour un administrateur.
 */
public final class SupplierErrors {

    private static final Logger log = LoggerFactory.getLogger(SupplierErrors.class);

    private SupplierErrors() {}

    public static SupplierException translate(String supplier, Exception e) {
        if (e instanceof SupplierException se) {
            return se;
        }

        log.warn("Erreur fournisseur {} : {}", supplier, e.toString());

        if (e instanceof HttpClientErrorException http) {
            int code = http.getStatusCode().value();

            if (code == 429 || code == 402) {
                return new SupplierException(SupplierErrorType.QUOTA_EXCEEDED, supplier,
                        supplier + " : quota d'utilisation atteint. Réessayez plus tard ou changez d'offre.");
            }
            if (code == 401 || code == 403) {
                return new SupplierException(SupplierErrorType.AUTH_FAILED, supplier,
                        supplier + " : authentification refusée. Vérifiez la clé API configurée.");
            }
            return new SupplierException(SupplierErrorType.INVALID_RESPONSE, supplier,
                    supplier + " : requête refusée (code " + code + ").");
        }

        if (e instanceof HttpServerErrorException) {
            return new SupplierException(SupplierErrorType.UNAVAILABLE, supplier,
                    supplier + " : le service rencontre une erreur. Réessayez plus tard.");
        }

        if (e instanceof ResourceAccessException) {
            return new SupplierException(SupplierErrorType.UNAVAILABLE, supplier,
                    supplier + " : service injoignable ou délai de réponse dépassé.");
        }

        if (e instanceof RestClientException) {
            return new SupplierException(SupplierErrorType.INVALID_RESPONSE, supplier,
                    supplier + " : réponse illisible ou dans un format inattendu.");
        }

        return new SupplierException(SupplierErrorType.UNAVAILABLE, supplier,
                supplier + " : erreur inattendue lors de l'appel.");
    }
}