Plan
===
Opis działania systemu
---
Funkcje
---
1. Zarządzaj kierownikami
2. Zarządzaj pracownikami
3. Autoryzacja
4. Zarządzaj zadaniami
5. Generowanie raportów
6. Zarządzanie profilem

### Ad. 1 ###
Tylko szef może zarządzać kierownikami.
Szef mianuje kierowników z pośród dostępnych pracowników.

Mianowanie Kierownika
* Jeżeli pracownik wykonuje zadanie, zadania wracają do kierownika.
* Pracownik zostaje mianowany kierownikiem.

Degradowanie kierownika:
* Wyznacz nowego kierownika
* Przekaż zadania nowemu kierownikowi
* Przyporządkuj pracowników nowemu kierownikowi
* Zamień kierownika w pracownika (ew. Zwolnij)

Zwolnienie kierownika:
* Wybierz nowego kierownika
* Przekaż zadania nowemu kierownikowi
* Użytkownik zaznaczony jako zwolniony

### Ad. 2 ###
Szef może zarządzać pracownikami przydziela pracowników swoim kierownikom.
Może zwalniać pracowników.
Zatwierdza kandydatów na pracowników.

Zwolnienie pracownika:
* Niedokończone zadania wracają do kierownika
* Użytkownik zaznaczony jako zwolniony

### Ad. 3 ###
Obsługa rejestracji i logowanie pracowników.

Informacje na temat autoryzacji:
* Logowanie loginem (Podawany podczas rejestracji)
* Hasła solone

Dane pracownicze:

1. E-mail
2. Hasło
3. Telefon
4. Imię
5. Nazwisko
6. PESEL
7. Płeć
8. Data urodzenia
9. Adres korespondencyjny
10. Login


Proces rejestracji:
* Kandydat rejestruje się podając niezbędne dane
* Komunikat podczas próby logowania
* Kandydat jest rejestrowany i oczekuje na zatwierdzenie jego kandydatury
* Szef zatwierdza kandydaturę
* Kandydat otrzymuje status pracownika

### Ad. 4 ###
 Zarządzanie zadaniami.
 Szef i kierownik mogą tworzyć zadania do wykonania.
 Szef tworzy zadania do wykonania dla kierownika.
 Kierownik na podstawie przydzielonych mu zadań tworzy zadania dla pracowników.

Szef:

1. Tworzenie zadań
2. Przydzielanie zadań
3. Monitorowanie zadań
4. Modyfikacja zadań (Czas zakończenia,Załączniki)

Kierownik:

1. Tworzenie zadań
2. Przydzielanie zadań
3. Monitorowanie zadań
4. Lista otrzymanych zadań do wykonania
6. Zgłaszanie problemów,opóźnień
7. Modyfikacja zadań (Czas zakończenia,Załączniki)

Pracownik:

4. Lista otrzymanych zadań do wykonania
5. Określanie postępu zadań
6. Zgłaszanie problemów,opóźnień

| |Tworzenie zadania  |Przydzielanie zadań  |Monitorowanie zadań  |Lista otrzymanych zadań  |Modyfikacja zadań  |Zgłaszanie problemów,opóźnień  |Określanie postępu zadań|
|---|---|---|---|---|---|---|---|
| **Szef**|Tak|Tak|Tak|Nie|Tak|Nie|Nie|
| **Kierownik**|Tak|Tak|Tak|Tak|Tak|Tak|Nie|
| **Pracownik**|Nie|Nie|Nie|Tak|Nie|Tak|Tak|

### Ad. 5 ###
Szef oraz Kierownik mogą generować raporty.
Pracownicy mogą wygenerować listę e-maili członków zespołu oraz listę nr telefonicznych.


|               |Raport końcowy|Raport z postępów |Lista mailingowa|Lista kontaktów|
|---------------|------------------|----------------|--------------|---------------|
|***Szef***     |Tak               |Tak             |Tak           |Tak            |
|***Kierownik***|Nie               |Tak*            |Tak*          |Tak*           |
|***Pracownik***|Nie               |Nie             |Tak*          |Tak*           |

\* - Ograniczenia

### Ad. 6 ###
Szef,Kierownik,Pracownik może modyfikować swój profil.
Blokada modyfikacji: PESEL
Modyfikacja imię/nazwisko poprzez załączenie odpowiednich dokumentów (kierownik)

Kierownik jest informowany gdy pracownik chce zmienić imię lub nazwisko.
