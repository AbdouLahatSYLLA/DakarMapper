# Fonction pour lire le contenu du fichier et transformer les lignes en tuples de chaînes
def lire_fichier(nom_fichier):
    with open(nom_fichier, 'r') as fichier:
        # Séparer les lignes en utilisant le saut de ligne comme délimiteur et créer un tuple pour chaque ligne
        donnees_tuple = tuple(fichier.read().strip().split('\n'))
        return donnees_tuple


# Appel de la fonction pour lire les données à partir du fichier
nom_fichier = "aftu_bus_coord"
donnees_tuple = lire_fichier(nom_fichier)

# Afficher le résultat
print(donnees_tuple)
