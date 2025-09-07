import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTextArea;

class Tabulka {
    Bunka[][] bunky = new Bunka[16][16];
    String pismena = "ABCDEFGHIJKLMNOP";
    JFrame okno = new JFrame("Excel.java");
    JTextArea textAreaTabulka = new JTextArea(10, 40);
    int[] velikosti = new int[16];

    Tabulka() {
        File soubor = new File("sesit.xcl");
        try {
            Scanner cist = new Scanner(soubor);
            for (byte radek = 0; radek < 16; radek++) {
                for (byte sloupec = 0; sloupec < 16; sloupec++) {
                    bunky[radek][sloupec] = new Bunka();
                    String data = cist.nextLine();
                    bunky[radek][sloupec].hodnota = data.split("\t")[0];
                    bunky[radek][sloupec].zarovnani = (byte) Integer.parseInt(data.split("\t")[1]);
                }
            }
            cist.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        sirkaSloupcu();
        okno.setSize(1400, 600);
        okno.getContentPane().add("Center", textAreaTabulka);
        okno.setVisible(true);
        textAreaTabulka.setFont(new Font("Courier New", 0, 12));
        textAreaTabulka.setBackground(Color.BLACK);
        textAreaTabulka.setForeground(Color.WHITE);
        textAreaTabulka.setCaretColor(Color.GREEN);
        textAreaTabulka.setSelectionColor(Color.GREEN);
        //textAreaTabulka.setEnabled(false);
        print();
        okno.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                String text = textAreaTabulka.getText();
                okno.dispose();
                text = text.split("\\|", 19)[18];
                byte sloupec = -1;
                byte radek = 0;
                for (String bunka: text.split("\\|")) {
                    if (sloupec == 16) {
                        sloupec = -1;
                        radek++;
                    }
                    sloupec++;
                    if (!bunka.contains("\n")) {
                        prepsat(sloupec, radek, bunka.trim());
                    }
                }
                ulozit();
            }
        });
    }

    public void print() {
        String strTabulka = "   |";
        for (byte sloupec = 0; sloupec < 16; sloupec++) { // názvy sloupců
            int sirka = (velikosti[sloupec] + 1) / 2;
            strTabulka += " ".repeat(sirka) + pismena.charAt(sloupec) + " ".repeat(velikosti[sloupec] - sirka + 1) + "|";
        }

        strTabulka += "\n";
        for (byte radek = 0; radek < 16; radek++) { // tabulka
            strTabulka += " ".repeat(2 - String.valueOf(radek).length()) + radek + " |";
            for (byte sloupec = 0; sloupec < 16; sloupec++) {
                Bunka bunka = bunky[radek][sloupec];
                int sirka = velikosti[sloupec] - bunka.hodnota.length() + 1;
                if (bunka.zarovnani == -1) {
                    strTabulka += " " + bunka.hodnota + " ".repeat(sirka) + "|";
                } else if (bunka.zarovnani == 1) {
                    strTabulka += " ".repeat(sirka) + bunka.hodnota + " |";
                } else {
                    int polosirka = sirka / 2;
                    strTabulka += " ".repeat(polosirka) + bunka.hodnota + " ".repeat(sirka - polosirka) + " |";
                };
            }
            strTabulka += "\n";
        }

        textAreaTabulka.setText(strTabulka);
    }

    public void sirkaSloupcu() {
        velikosti = new int[16];
        for (byte radek = 0; radek < 16; radek++) { // výpočet šířky sloupců
            for (byte sloupec = 0; sloupec < 16; sloupec++) {
                velikosti[sloupec] = Math.max(5, Math.max(velikosti[sloupec], bunky[radek][sloupec].hodnota.length()));
            }
        }
    }

    public void prepsat(byte sloupec, byte radek, String hodnota) {
        bunky[radek][sloupec].hodnota = hodnota.replace("\t", "");
    }

    public void zarovnej(String sloupec, byte radek, byte zarovnani) {
        bunky[radek][pismena.indexOf(sloupec)].zarovnani = zarovnani;
        ulozit();
    }

    public void ulozit() {
        try {
            FileWriter zapsat = new FileWriter("sesit.xcl");
            for (byte radek = 0; radek < 16; radek++) {
                for (byte sloupec = 0; sloupec < 16; sloupec++) {
                    zapsat.write(bunky[radek][sloupec].hodnota + "\t" + bunky[radek][sloupec].zarovnani + "\n");
                }
            }
            zapsat.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
