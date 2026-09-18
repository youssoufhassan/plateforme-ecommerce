# Variables de développement local — NE PAS COMMITER avec de vraies valeurs
$env:JWT_SECRET = "43cb68884dc37b832d218ee791f383574d0dc6c43e67a952"
$env:STRIPE_SECRET_KEY = "sk_test_51UDsbkJDHBwZKWsE1jKwgT7orY6RXeu6TwVPvwwOrEWbjEANT9l1U4BDZwXBaunFmMJ4oYemrAkOcaSpVTPw7yuF00syI0Qnur"
$env:STRIPE_WEBHOOK_SECRET = "whsec_df64d53d5263921fb7abf35d226ce57833c5c7c93351b579"
$env:FRAGELLA_API_KEY = "votre_cle_fragella"

mvn spring-boot:run