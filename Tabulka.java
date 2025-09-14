import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

class Tabulka {
    Bunka[][] bunky = new Bunka[16][16];
    String pismena = "ABCDEFGHIJKLMNOP";
    JFrame okno = new JFrame("Excel.java");
    JTextArea textAreaTabulka = new JTextArea(10, 40);
    int[] velikosti = new int[16];
    boolean ignorovatZmeny = false;
    int kurzor = 0;

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
        textAreaTabulka.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!ignorovatZmeny) {
                    kurzor = textAreaTabulka.getCaretPosition() + 1;
                    SwingUtilities.invokeLater(() -> {update();});
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!ignorovatZmeny) {
                    kurzor = textAreaTabulka.getCaretPosition() - 1;
                    SwingUtilities.invokeLater(() -> {update();});
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!ignorovatZmeny) {
                    kurzor = textAreaTabulka.getCaretPosition();
                    SwingUtilities.invokeLater(() -> {update();});
                }
            }
        });
        okno.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                okno.dispose();
                ulozit();
            }
        });
    }

    public String mezery(int n) {
        if (n == 0) {
            return "";
        }
        String s = " ";
        for (int i = 1; i < n; i++) {
            s += " ";
        }
        return s;
    }

    public int sum(int[] x) {
        int s = 0;
        for (int y: x) {
            s += y;
        }
        return s;
    }

    public void update() {
        String text = textAreaTabulka.getText();
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
        sirkaSloupcu();
        print();
        textAreaTabulka.setCaretPosition(kurzor);
    }

    public void print() {
        String strTabulka = "   |";
        for (byte sloupec = 0; sloupec < 16; sloupec++) { // názvy sloupců
            int sirka = (velikosti[sloupec] + 1) / 2;
            strTabulka += mezery(sirka) + pismena.charAt(sloupec) + mezery(velikosti[sloupec] - sirka + 1) + "|";
        }

        strTabulka += "\n";
        for (byte radek = 0; radek < 16; radek++) { // tabulka
            strTabulka += mezery(2 - String.valueOf(radek).length()) + radek + " |";
            for (byte sloupec = 0; sloupec < 16; sloupec++) {
                Bunka bunka = bunky[radek][sloupec];
                int sirka = velikosti[sloupec] - bunka.hodnota.length() + 1;
                if (bunka.zarovnani == -1) {
                    strTabulka += " " + bunka.hodnota + mezery(sirka) + "|";
                } else if (bunka.zarovnani == 1) {
                    strTabulka += mezery(sirka) + bunka.hodnota + " |";
                } else {
                    int polosirka = sirka / 2;
                    strTabulka += mezery(polosirka) + bunka.hodnota + mezery(sirka - polosirka) + " |";
                };
            }
            strTabulka += "\n";
        }
        ignorovatZmeny = true;
        textAreaTabulka.setText(strTabulka);
        ignorovatZmeny = false;
    }

    public void sirkaSloupcu() {
        int[] velikosti2 = velikosti.clone();
        velikosti = new int[16];
        for (byte radek = 0; radek < 16; radek++) { // výpočet šířky sloupců
            for (byte sloupec = 0; sloupec < 16; sloupec++) {
                velikosti[sloupec] = Math.max(5, Math.max(velikosti[sloupec], bunky[radek][sloupec].hodnota.length()));
            }
        }
        kurzor += (sum(velikosti) - sum(velikosti2)) * ((int) kurzor / (sum(velikosti) + 52));
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
