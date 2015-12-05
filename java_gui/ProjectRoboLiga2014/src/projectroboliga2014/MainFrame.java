/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package projectroboliga2014;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;

/**
 *
 * @author Tilen
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    
    
    //Default parameters for connection
    private String connectionURL = "192.168.1.10";
    private String nameRobot = "MaSheen";
    private String passRobot = "charlie";
    private String dataRobot = "Zivjo";
    
    /** true if we are in drag */
    private boolean inDrag = false; 
    
    /** starting location of a drag */
    private int startX = -1, startY = -1;

    /** current location of a drag */
    private int curX = -1, curY = -1;    
    
    /** firstly selected coordinates **/
    public int firstX = 1;
    public int firstY = 1;
    
    public int secondX = 1;
    public int secondY = 1;
    
    /** Counter to know which coordinates we're on 
     * If it's -1, or 0 it means we are selecting field size
     * If it's on 1 it's the first selected point
     * anything more means it's second/third/... point
    **/
    private int mainCounter = -2;
    
    /** Determining size of map*/
    int leftX = 0;
    int rightX = 0;
    
    /** Plate number */
    int tileNumber = 9;
    
    /** centimeters in coordinates value */
    double cmInCoords = 5;
    
    
    /** Current heading of the robot **/
    private int currentHeading = 0;
    
    /** Saved heading for delete last button **/
    private int deleteAllHeading = 0;
    
    /** Saved heading for deleting last button **/
    private int deleteLastHeading = 0;
    
    /** Sending coordinates to front panel for drawing **/
    private String coordinatesToDraw = "";
    
    /** ScreenWidth and Height **/
    private int screenWidth = 500;
    private int screenHeight = 500;
    
    public MainFrame() {
        
        initComponents();
        
        //Get size of each screen
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice g : gs) {
            DisplayMode dm = g.getDisplayMode();
            screenWidth = dm.getWidth();
            screenHeight = dm.getHeight()-40;
        }
	//End of screen gathering info
        
        setBounds(0, 0, screenWidth, screenHeight);
        
        
        Timer timer = new Timer();
        
        TimerTask task; 
        task = new TimerTask() 
        {
            @Override
            public void run()
            {
                satScanRequest();
                try {
                    File satScan = new File("satscan.jpg");
                    backPane.openFile(satScan);
                } catch (Exception e) {
                }             
            }
        };
        timer.scheduleAtFixedRate(task, 0, 600); // 1000ms = 1s
    }
    
    private void satScanRequest() {
        try {
            //-------------------------------------------------------
            // PARAMETRI ZA POŠILJANJE
            //-------------------------------------------------------
            // Naslov streznika in pot do programa za posiljanje podatkov
            String urlImage = "http://"+connectionURL+"/satNeXT/satscan.jpg";
            //---------------------------------------------------
            //---------------------------------------------------
            // Ustvarimo nov objekt - povezavo HTTP. Nato beremo
            // datoteko s sliko po kosih velikosti 1024.
            URL url = new URL(urlImage);
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while ((n=in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();

            // Prebrane bajte zapisemo se v datoteko na disku.
            FileOutputStream fos = new FileOutputStream("satscan.jpg");
            fos.write(response);
            fos.close();
        } catch (IOException e){
            System.out.println(connectionURL);
        }
    }
    
    
    private void sendHTTP(String url, String urlParameters) throws Exception {
		
	// Ustvarimo nov objekt - povezavo HTTP.
	URL obj = new URL(url);
	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
	// metoda posiljanja: "GET" ali "POST"
	con.setRequestMethod("POST");
		
	// Posljemo zahtevo s parametri
	con.setDoOutput(true);
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(urlParameters);
	wr.flush();
	wr.close();
		
	//---------------------------------------------------
	// PREJEMANJE ODGOVORA S STREŽNIKA
	//---------------------------------------------------
	// Izpišimo strežnikov odgovor.
	//   - Če je bil zahtevek uspešno obdelan, bo strežnik vrnil število paketov,
	//     ki jih je vaš robot prejel.
	//   - Če je odgovor -1, pomeni, da je prišlo do napake ali pa da tekma še ne teče.
	//     Preverite pravilnost imena robota, gesla, naslova strežnika.
		
	// Odgovor streznika - koda
	int responseCode = con.getResponseCode();
		
	// Preberemo ostale podatke, ki jih je streznik vrnil.
	BufferedReader in = new BufferedReader(
		new InputStreamReader(con.getInputStream()));
	String inputLine;
        StringBuffer response = new StringBuffer();
	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();
		
	// Izpisemo poslano zahtevo in odgovor streznika.
	System.out.println("\nPoslana zahteva:\n" + url + "?" + urlParameters);
	System.out.println("Odgovor streznika - koda:\n" + responseCode);
	System.out.println("Odgovor streznika - podatki:\n" + response.toString());
        recvSendText.setText(recvSendText.getText()+"Poslana zahteva: " + url + "?" + urlParameters+"\n");
	recvSendText.setText(recvSendText.getText()+"Odgovor streznika - koda: " + responseCode+"\n");
	recvSendText.setText(recvSendText.getText()+"Odgovor streznika - podatki: " + response.toString()+"\n\n");
        
        if (response.toString().equals("-1")) {
            sendHTTP(url, urlParameters);
        }
    }
    private void sendHTTPforResponse(String url, String urlParameters) throws Exception {
		
		// Ustvarimo nov objekt - povezavo HTTP.
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		// metoda posiljanja: "GET" ali "POST"
		con.setRequestMethod("POST");
		
		// Posljemo zahtevo s parametri
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		// Izpisemo poslano zahtevo.
		System.out.println("Poslana zahteva:\n" + url + "?" + urlParameters);
		
		//------------------------------------------------------------------------------------
		// PREJEMANJE ODGOVORA S STREŽNIKA
		//------------------------------------------------------------------------------------
		// Izpišimo strežnikov odgovor.
		//   - Če je odgovor prazen (null) pomeni nekaj od naslednjega:
		//        -> tekma še ne teče
		//        -> vaš robot v tekmi ne sodeluje
		//        -> na vas ne čaka noben paket, čeprav robot sodeluje v tekmi
		//        -> prišlo je do kakšne druge napake. Preverite pravilnost imena robota,
		//           gesla, naslova strežnika.
		//
		//   - Če je bil zahtevek uspešno obdelan, bo strežnik vrnil paket posebne oblike:
		//   +--------------------+------------------+-------------- ... -------------+
		//   | 4 B: št. prebranih | 4 B: časovni žig | do 50 B: podatki               |
		//   +--------------------+------------------+-------------- ... -------------+
		//
		//   -> V prvih 4 bajtih je celo število, ki pove, koliko paketov ste že prevzeli
		//      od robota.
		//   -> V naslednjih 4 bajtih je celo število, ki predstavlja časovni žig v formatu
		//      UNIX. Gre za število sekund, ki je preteklo od 1. januarja 1970, 00:00:00.
		//   -> naslednjih nekaj bajtov je namenjenih podatkom, ki jih je poslal robot.
		//      Teh podatkov je največ za 50 bajtov.
		
		// Odgovor streznika - koda
		int responseCode = con.getResponseCode();
		
		// Preberemo ostale podatke, ki jih je streznik vrnil.
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		// Odgovor streznika - paket - shranimo v niz znakov. 
		String responseStr = response.toString();
		// Niz znakov pretvorimo v tabelo bajtov.
		byte[] responseBytes = responseStr.getBytes();
		
		// Dolzina paketa
		int responseLength = response.length();
		
		System.out.println("Odgovor streznika:\nKoda "+ responseCode);
		
		// Testni izpis vsebine v sestnajstiskem zapisu
		System.out.println("Dolzina paketa: "+responseLength);
		System.out.print("Vsebina (HEX): ");
		for (int i=0;i<responseLength;i++){
			System.out.print(String.format("%x ", responseBytes[i]));
		}
		System.out.println();
		
		// Dolzina podatkovnega dela
		int dataLength = response.length()-8;
		
		
		if (dataLength >= 0){
			System.out.println("Vsebina paketa po delih:");
			// Posamezne dele paketa pretvorimo v ustrezni format.
			// Prvi 4 bajti: stevilo prebranih paketov.
			byte[] arr = Arrays.copyOfRange(responseBytes, 0, 4);
			ByteBuffer wrapped = ByteBuffer.wrap(arr); // privzeto pravilo debelega konca (big-endian)
			int numRead = wrapped.getInt();
		
			// Drugi 4 bajti: casovni zig.
			arr = Arrays.copyOfRange(responseBytes, 4, 8);
			wrapped = ByteBuffer.wrap(arr);
			int timestamp = wrapped.getInt();
			
			// Preostanek: podatki.
			arr = Arrays.copyOfRange(responseBytes, 8, responseLength);
			wrapped = ByteBuffer.wrap(arr);
			String data = new String(arr);
			
			System.out.println("  Stevilo prevzetih paketov: "+numRead);
			System.out.println("  Casovni zig paketa: "+timestamp);
			System.out.println("  Podatki: "+data);
                        recvSendText.setText(recvSendText.getText()+" Stevilo prevzetih paketov: "+numRead+"\nCasovni zig paketa: "+timestamp+"\nPodatki: "+data+"\n\n");
		}
		else{
			System.out.println("Nisem prejel veljavnega paketa.\n\n");
		}
		
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sendRecievedFrame = new javax.swing.JFrame();
        sendRecievedPane = new javax.swing.JPanel();
        specClose = new javax.swing.JButton();
        specSend = new javax.swing.JButton();
        specRecv = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        recvSendText = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        toSendText = new javax.swing.JTextArea();
        labSent = new javax.swing.JLabel();
        labTextToSend = new javax.swing.JLabel();
        connectionFrame = new javax.swing.JFrame();
        changeConPane = new javax.swing.JPanel();
        currentCon = new javax.swing.JPanel();
        labChange1 = new javax.swing.JLabel();
        labCurrentIP1 = new javax.swing.JLabel();
        labCurrentName1 = new javax.swing.JLabel();
        labCurrentPass1 = new javax.swing.JLabel();
        labCurrentIP2 = new javax.swing.JLabel();
        labCurrentName2 = new javax.swing.JLabel();
        labCurrentPass2 = new javax.swing.JLabel();
        labConnectionFrame = new javax.swing.JLabel();
        conClose = new javax.swing.JButton();
        conSaveClose = new javax.swing.JButton();
        conSave = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        labChange2 = new javax.swing.JLabel();
        labChangeIP1 = new javax.swing.JLabel();
        labChangeName1 = new javax.swing.JLabel();
        labChangePass1 = new javax.swing.JLabel();
        textChangeIP = new javax.swing.JTextField();
        textChangeName = new javax.swing.JTextField();
        textChangePass = new javax.swing.JTextField();
        mapSettingsFrame = new javax.swing.JFrame();
        labMapSettingsFrame = new javax.swing.JLabel();
        labCurrPlates = new javax.swing.JLabel();
        curPlates = new javax.swing.JLabel();
        labChangePlates = new javax.swing.JLabel();
        changePlates = new javax.swing.JTextField();
        mapSettingsClose = new javax.swing.JButton();
        mapSettingsSave = new javax.swing.JButton();
        mapSettingsSaveClose = new javax.swing.JButton();
        mapSettingsReset = new javax.swing.JButton();
        labReset = new javax.swing.JLabel();
        backPane = new projectroboliga2014.BackgroundPane();
        labBackStatus = new javax.swing.JLabel();
        mainMenu = new javax.swing.JMenuBar();
        orders = new javax.swing.JMenu();
        send = new javax.swing.JMenuItem();
        Recieve = new javax.swing.JMenuItem();
        deleteLast = new javax.swing.JMenuItem();
        deleteAll = new javax.swing.JMenuItem();
        sentRecieved = new javax.swing.JMenuItem();
        commands = new javax.swing.JMenu();
        beep = new javax.swing.JMenuItem();
        turnRobot180 = new javax.swing.JMenuItem();
        Back = new javax.swing.JMenuItem();
        downArm = new javax.swing.JMenuItem();
        autoGrab = new javax.swing.JMenuItem();
        down = new javax.swing.JMenuItem();
        up = new javax.swing.JMenuItem();
        resetHeading = new javax.swing.JMenuItem();
        mapSettings = new javax.swing.JMenuItem();
        connection = new javax.swing.JMenu();
        changeCon = new javax.swing.JMenuItem();
        help = new javax.swing.JMenu();
        about = new javax.swing.JMenuItem();

        sendRecievedFrame.setTitle("CommmandsInfo");
        sendRecievedFrame.getContentPane().setLayout(new javax.swing.OverlayLayout(sendRecievedFrame.getContentPane()));

        sendRecievedPane.setPreferredSize(new java.awt.Dimension(700, 450));
        sendRecievedPane.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        specClose.setText("Close");
        specClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specCloseActionPerformed(evt);
            }
        });
        sendRecievedPane.add(specClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 400, 125, 40));

        specSend.setText("Send");
        specSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specSendActionPerformed(evt);
            }
        });
        sendRecievedPane.add(specSend, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 400, 125, 40));

        specRecv.setText("Recieve");
        specRecv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specRecvActionPerformed(evt);
            }
        });
        sendRecievedPane.add(specRecv, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 400, 125, 40));

        recvSendText.setEditable(false);
        recvSendText.setColumns(20);
        recvSendText.setRows(5);
        jScrollPane2.setViewportView(recvSendText);

        sendRecievedPane.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 40, 340, 350));

        toSendText.setColumns(20);
        toSendText.setRows(5);
        toSendText.setPreferredSize(new java.awt.Dimension(340, 350));
        jScrollPane1.setViewportView(toSendText);
        toSendText.setLineWrap(true);

        sendRecievedPane.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 340, 350));

        labSent.setText("Sent/Recieved text");
        sendRecievedPane.add(labSent, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, -1, -1));

        labTextToSend.setText("Text to send");
        sendRecievedPane.add(labTextToSend, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 20, 90, -1));

        sendRecievedFrame.getContentPane().add(sendRecievedPane);
        setVisible(false);

        changeConPane.setPreferredSize(new java.awt.Dimension(500, 350));
        changeConPane.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        currentCon.setBackground(new java.awt.Color(204, 204, 204));
        currentCon.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labChange1.setText("Current connection details");
        currentCon.add(labChange1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, -1, -1));

        labCurrentIP1.setText("IP:");
        currentCon.add(labCurrentIP1, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 30, 65, 23));

        labCurrentName1.setText("Robot Name:");
        currentCon.add(labCurrentName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 60, 65, 23));

        labCurrentPass1.setText("Robot Pass:");
        currentCon.add(labCurrentPass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 90, 65, 23));

        labCurrentIP2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labCurrentIP2.setText("a");
        currentCon.add(labCurrentIP2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 230, 23));
        labCurrentIP2.setText(connectionURL);

        labCurrentName2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labCurrentName2.setText("aa");
        currentCon.add(labCurrentName2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 60, 230, 20));
        labCurrentName2.setText(nameRobot);

        labCurrentPass2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labCurrentPass2.setText("aaa");
        currentCon.add(labCurrentPass2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 90, 230, 23));
        labCurrentPass2.setText(passRobot);

        changeConPane.add(currentCon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 520, 120));

        labConnectionFrame.setText("Change connection window");
        changeConPane.add(labConnectionFrame, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 10, -1, -1));

        conClose.setText("Close");
        conClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conCloseActionPerformed(evt);
            }
        });
        changeConPane.add(conClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 330, 125, 30));

        conSaveClose.setText("Save and close");
        conSaveClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conSaveCloseActionPerformed(evt);
            }
        });
        changeConPane.add(conSaveClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 330, 125, 30));

        conSave.setText("Save");
        conSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conSaveActionPerformed(evt);
            }
        });
        changeConPane.add(conSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 125, 30));

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labChange2.setText("Changing details window");
        jPanel1.add(labChange2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, -1, -1));

        labChangeIP1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labChangeIP1.setText("IP:");
        jPanel1.add(labChangeIP1, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 30, 65, 23));

        labChangeName1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labChangeName1.setText("Robot Name:");
        jPanel1.add(labChangeName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 60, 65, 23));

        labChangePass1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labChangePass1.setText("Robot Pass");
        jPanel1.add(labChangePass1, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 90, 65, 23));

        textChangeIP.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jPanel1.add(textChangeIP, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 230, 23));

        textChangeName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jPanel1.add(textChangeName, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 60, 230, 23));

        textChangePass.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jPanel1.add(textChangePass, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 90, 230, 23));

        changeConPane.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 520, 130));

        javax.swing.GroupLayout connectionFrameLayout = new javax.swing.GroupLayout(connectionFrame.getContentPane());
        connectionFrame.getContentPane().setLayout(connectionFrameLayout);
        connectionFrameLayout.setHorizontalGroup(
            connectionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
            .addGroup(connectionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(connectionFrameLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(changeConPane, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        connectionFrameLayout.setVerticalGroup(
            connectionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
            .addGroup(connectionFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(connectionFrameLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(changeConPane, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        setVisible(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        connectionFrame.setSize(600,425);
        connectionFrame.setLocation(dim.width/2-connectionFrame.getSize().width/2, dim.height/2-connectionFrame.getSize().height/2);

        mapSettingsFrame.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labMapSettingsFrame.setText("Map settings frame");
        mapSettingsFrame.getContentPane().add(labMapSettingsFrame, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, -1, -1));

        labCurrPlates.setText("Current number of plates in a row");
        mapSettingsFrame.getContentPane().add(labCurrPlates, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, 23));

        curPlates.setText("Abra");
        mapSettingsFrame.getContentPane().add(curPlates, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 40, 120, 23));
        curPlates.setText(""+tileNumber);

        labChangePlates.setText("Change number of plates in a row");
        mapSettingsFrame.getContentPane().add(labChangePlates, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, 23));
        mapSettingsFrame.getContentPane().add(changePlates, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 70, 120, 23));

        mapSettingsClose.setText("Close");
        mapSettingsClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSettingsCloseActionPerformed(evt);
            }
        });
        mapSettingsFrame.getContentPane().add(mapSettingsClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 100, 100, -1));

        mapSettingsSave.setText("Save");
        mapSettingsSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSettingsSaveActionPerformed(evt);
            }
        });
        mapSettingsFrame.getContentPane().add(mapSettingsSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 100, -1));

        mapSettingsSaveClose.setText("Save and close");
        mapSettingsSaveClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSettingsSaveCloseActionPerformed(evt);
            }
        });
        mapSettingsFrame.getContentPane().add(mapSettingsSaveClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 100, 115, -1));

        mapSettingsReset.setText("Reset edges");
        mapSettingsReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSettingsResetActionPerformed(evt);
            }
        });
        mapSettingsFrame.getContentPane().add(mapSettingsReset, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 130, -1, -1));
        mapSettingsFrame.getContentPane().add(labReset, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 130, 80, 23));

        setVisible(false);
        mapSettingsFrame.setSize(400,200);
        mapSettingsFrame.setLocation(dim.width/2-mapSettingsFrame.getSize().width/2, dim.height/2-mapSettingsFrame.getSize().height/2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.OverlayLayout(getContentPane()));

        backPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backPaneMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                backPaneMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                backPaneMouseReleased(evt);
            }
        });
        backPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                backPaneMouseDragged(evt);
            }
        });
        backPane.setLayout(null);

        labBackStatus.setBackground(new java.awt.Color(255, 255, 255));
        labBackStatus.setText("Status label");
        labBackStatus.setOpaque(true);
        backPane.add(labBackStatus);
        labBackStatus.setBounds(10, 400, 670, 23);
        //Get size of each screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice g : gs) {
            DisplayMode dm = g.getDisplayMode();
            screenWidth = dm.getWidth();
            screenHeight = dm.getHeight()-40;
        }
        //End of screen gathering info

        labBackStatus.setLocation(10, 5);
        //labBackStatus.setLocation(10, screenHeight-70);
        labBackStatus.setSize(screenWidth-50, 23);
        labBackStatus.setText("Current map tiles count: "+tileNumber);

        System.err.println(screenHeight-30+" "+screenHeight);

        getContentPane().add(backPane);
        backPane.setFocusable(true);

        orders.setMnemonic('O');
        orders.setText("Orders");

        send.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0));
        send.setMnemonic('e');
        send.setText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });
        orders.add(send);

        Recieve.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        Recieve.setMnemonic('r');
        Recieve.setText("Recieve");
        Recieve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RecieveActionPerformed(evt);
            }
        });
        orders.add(Recieve);

        deleteLast.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        deleteLast.setMnemonic('t');
        deleteLast.setText("Delete last");
        deleteLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLastActionPerformed(evt);
            }
        });
        orders.add(deleteLast);

        deleteAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        deleteAll.setMnemonic('a');
        deleteAll.setText("Delete all");
        deleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllActionPerformed(evt);
            }
        });
        orders.add(deleteAll);

        sentRecieved.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        sentRecieved.setMnemonic('S');
        sentRecieved.setText("Sent/Recieved");
        sentRecieved.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sentRecievedActionPerformed(evt);
            }
        });
        orders.add(sentRecieved);

        mainMenu.add(orders);

        commands.setMnemonic('S');
        commands.setText("Robot commands");
        commands.setToolTipText("");

        beep.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, 0));
        beep.setMnemonic('B');
        beep.setText("Beep");
        beep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                beepActionPerformed(evt);
            }
        });
        commands.add(beep);

        turnRobot180.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, 0));
        turnRobot180.setText("Turn 180");
        turnRobot180.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                turnRobot180ActionPerformed(evt);
            }
        });
        commands.add(turnRobot180);

        Back.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, 0));
        Back.setText("Back 20");
        Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackActionPerformed(evt);
            }
        });
        commands.add(Back);

        downArm.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
        downArm.setText("Down arm");
        downArm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downArmActionPerformed(evt);
            }
        });
        commands.add(downArm);

        autoGrab.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, 0));
        autoGrab.setText("Auto-Grab");
        autoGrab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoGrabActionPerformed(evt);
            }
        });
        commands.add(autoGrab);

        down.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0));
        down.setMnemonic('D');
        down.setText("Release item");
        down.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downActionPerformed(evt);
            }
        });
        commands.add(down);

        up.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, 0));
        up.setMnemonic('U');
        up.setText("Up");
        up.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upActionPerformed(evt);
            }
        });
        commands.add(up);

        resetHeading.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        resetHeading.setText("Reset heading");
        resetHeading.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetHeadingActionPerformed(evt);
            }
        });
        commands.add(resetHeading);

        mapSettings.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        mapSettings.setText("Map settings");
        mapSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapSettingsActionPerformed(evt);
            }
        });
        commands.add(mapSettings);

        mainMenu.add(commands);

        connection.setMnemonic('C');
        connection.setText("Connection");

        changeCon.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        changeCon.setMnemonic('e');
        changeCon.setText("Change con");
        changeCon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeConActionPerformed(evt);
            }
        });
        connection.add(changeCon);

        mainMenu.add(connection);

        help.setMnemonic('H');
        help.setText("Help");

        about.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        about.setMnemonic('a');
        about.setText("About");
        about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutActionPerformed(evt);
            }
        });
        help.add(about);

        mainMenu.add(help);

        setJMenuBar(mainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sentRecievedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sentRecievedActionPerformed
        // TODO add your handling code here:
        sendRecievedFrame.setVisible(true);
        sendRecievedFrame.setSize(730, 500);
    }//GEN-LAST:event_sentRecievedActionPerformed

    private void specCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specCloseActionPerformed
        // TODO add your handling code here:
        sendRecievedFrame.setVisible(false);
    }//GEN-LAST:event_specCloseActionPerformed

    private void aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this, "Made by: Avsec Tilen, Tovornik Robert, Putrle Ziga");
    }//GEN-LAST:event_aboutActionPerformed

    private void changeConActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeConActionPerformed
        // TODO add your handling code here:
        connectionFrame.setVisible(true);
    }//GEN-LAST:event_changeConActionPerformed

    private void conSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conSaveActionPerformed
        // TODO add your handling code here:
        saveConnection();
    }//GEN-LAST:event_conSaveActionPerformed

    private void conSaveCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conSaveCloseActionPerformed
        // TODO add your handling code here:
        saveConnection();
        connectionFrame.setVisible(false);
    }//GEN-LAST:event_conSaveCloseActionPerformed

    private void conCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conCloseActionPerformed
        // TODO add your handling code here:
        connectionFrame.setVisible(false);
    }//GEN-LAST:event_conCloseActionPerformed

    private void beepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beepActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("B");
    }//GEN-LAST:event_beepActionPerformed

    private void downActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("D");
    }//GEN-LAST:event_downActionPerformed

    private void upActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("U");
    }//GEN-LAST:event_upActionPerformed

    /** Function for moving the robot  // actually for adding R/L and M command **/
    private void moveYourAssRobot() {
        //Path required to travel
        int path = pathLength(firstX, firstY, secondX, secondY, cmInCoords);
        
        //Heading from current point to next
        int requiredHeading = determineHeading(firstX, firstY, secondX, secondY);
        
        /** Calculating required turn of the robot*/
        if (currentHeading != requiredHeading) {
            turn(currentHeading, requiredHeading); 
            System.err.println(currentHeading+"  "+requiredHeading);
            currentHeading = requiredHeading;
        }
        
        /** Adding path */
        addDataToTextArea("M"+path);

        /** painting path*/
        backPane.iLovePainting(coordinatesToDraw);
    }
    
    private void sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendActionPerformed
        // TODO add your handling code here:
        sendRobotData();
    }//GEN-LAST:event_sendActionPerformed

    private void RecieveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RecieveActionPerformed
        // TODO add your handling code here:
        recieveRobotData();
    }//GEN-LAST:event_RecieveActionPerformed

    private void specRecvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specRecvActionPerformed
        // TODO add your handling code here:
        recieveRobotData();
    }//GEN-LAST:event_specRecvActionPerformed

    private void specSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specSendActionPerformed
        // TODO add your handling code here:
        sendRobotData();
    }//GEN-LAST:event_specSendActionPerformed

    private void deleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllActionPerformed
        // TODO add your handling code here:
        toSendText.setText("");
        coordinatesToDraw = "";
        mainCounter = 0;
        currentHeading = deleteAllHeading;
        labBackStatus.setText("Current heading is: "+currentHeading);
    }//GEN-LAST:event_deleteAllActionPerformed

    private void deleteLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLastActionPerformed
        // TODO add your handling code here:
        if (toSendText.getText().isEmpty() != true) {
            String commands = toSendText.getText();
            String delim = "-";
            String[] tokens = commands.split(delim);
            int maxLen = tokens.length;
            toSendText.setText("");
            
            //Ce je ukaz za obrat (R ali L) spremeni heading na prejsnji heading
            String deletedFirstChar = Character.toString(tokens[maxLen-1].charAt(0));
            if ("R".equals(deletedFirstChar) || "L".equals(deletedFirstChar)) {
                currentHeading = deleteLastHeading;
            }
            if ("M".equals(deletedFirstChar)) {
                String[] tokens2 = coordinatesToDraw.split(delim);
                int maxLen2 = tokens2.length;
                coordinatesToDraw = "";
                for (int i = 0; i < maxLen-2; i++) {
                    addDataToCoordinates(tokens2[i]);
                }
            }
            
            for (int i = 0; i < maxLen-1; i++) {
                addDataToTextArea(tokens[i]);
            }
        }
    }//GEN-LAST:event_deleteLastActionPerformed

    private void mapSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSettingsActionPerformed
        // TODO add your handling code here:
        mapSettingsFrame.setVisible(true);
    }//GEN-LAST:event_mapSettingsActionPerformed

    private void mapSettingsCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSettingsCloseActionPerformed
        // TODO add your handling code here:
        mapSettingsFrame.setVisible(false);
        labReset.setText("");
    }//GEN-LAST:event_mapSettingsCloseActionPerformed

    private void mapSettingsSaveCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSettingsSaveCloseActionPerformed
        // TODO add your handling code here:
        if (changePlates.getText().isEmpty() != true) {
            tileNumber = Integer.parseInt(changePlates.getText());
            curPlates.setText(""+tileNumber);
            changePlates.setText("");
        } else {
            System.err.println("Error");
        }
        labReset.setText("");
        mapSettingsFrame.setVisible(false);
    }//GEN-LAST:event_mapSettingsSaveCloseActionPerformed

    private void mapSettingsSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSettingsSaveActionPerformed
        // TODO add your handling code here:
        if (changePlates.getText().isEmpty() != true) {
            tileNumber = Integer.parseInt(changePlates.getText());
            curPlates.setText(""+tileNumber);
            changePlates.setText("");
        } else {
            System.err.println("Error");
        }
    }//GEN-LAST:event_mapSettingsSaveActionPerformed

    private void mapSettingsResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapSettingsResetActionPerformed
        // TODO add your handling code here:
        //Now you can set edges of the map again
        mainCounter = -2;
        labReset.setText("Resetted!");
    }//GEN-LAST:event_mapSettingsResetActionPerformed

    private void resetHeadingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetHeadingActionPerformed
        // TODO add your handling code here:
        inDrag = true;
    }//GEN-LAST:event_resetHeadingActionPerformed

    private void autoGrabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoGrabActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("G");
    }//GEN-LAST:event_autoGrabActionPerformed

    private void BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("H2");
        addDataToTextArea("M15");
    }//GEN-LAST:event_BackActionPerformed

    private void turnRobot180ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_turnRobot180ActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("R180");
    }//GEN-LAST:event_turnRobot180ActionPerformed

    private void downArmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downArmActionPerformed
        // TODO add your handling code here:
        addDataToTextArea("S");
    }//GEN-LAST:event_downArmActionPerformed

    private void backPaneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backPaneMousePressed
        // TODO add your handling code here:
        Point p = evt.getPoint();
        //System.out.println("mousePressed at " + p);
        startX = p.x;
        startY = p.y;
        //inDrag = true;
    }//GEN-LAST:event_backPaneMousePressed

    private void backPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backPaneMouseReleased
        // TODO add your handling code here:
        /*
        System.out.println("SELECTION IS " + startX + "," + startY + " to "
            + curX + "," + curY);
        */
        if (inDrag) {
            currentHeading = determineHeading(startX, startY, curX, curY);
            System.out.println("Selected heading is: "+currentHeading);
            labBackStatus.setText(labBackStatus.getText()+ "   Selected heading is: " +currentHeading);
            inDrag = false;
        }
    }//GEN-LAST:event_backPaneMouseReleased

    private void backPaneMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backPaneMouseDragged
        // TODO add your handling code here:
        Point p = evt.getPoint();
        //System.out.println("mouse Dragged to " + p);
        curX = p.x;
        curY = p.y;
        inDrag = true;
        /*
        if (inDrag) {
            System.out.println("start: "+startX+" "+startY+" end: "+curX+" "+curY);
        }
        */
    }//GEN-LAST:event_backPaneMouseDragged

    private void backPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backPaneMouseClicked
        // TODO add your handling code here:
        int clickedX = evt.getX();
        int clickedY = evt.getY();
        if (!inDrag) {
            mainCounter++;
            if (mainCounter == 1) {
                //System.out.println(mainCounter);
                firstX = clickedX;
                firstY = clickedY;
                addDataToCoordinates(""+clickedX);
                addDataToCoordinates(""+clickedY);
                deleteAllHeading = currentHeading;
            } else if (mainCounter == -1) {
                System.out.println(mainCounter);
                leftX = clickedX;              
                labBackStatus.setText("Left X has been selected. |||| Current tile number is: "+tileNumber);
            } else if (mainCounter == 0) { 
                System.out.println(mainCounter);
                rightX = clickedX;
                cmInCoords = coordToCM(leftX, rightX, tileNumber);
                labBackStatus.setText("Left and right X have been selected. Cm in coords is: "+cmInCoords);
                //System.out.println(cmInCoords);
            }else if (mainCounter > 1) {
                System.out.println(mainCounter);
                secondX = clickedX;
                secondY = clickedY;
                addDataToCoordinates(""+clickedX);
                addDataToCoordinates(""+clickedY);
                deleteLastHeading = currentHeading;
                moveYourAssRobot();
                firstX = clickedX;
                firstY = clickedY;
            } else {
                System.out.println("Napaka");
            }
        }
    }//GEN-LAST:event_backPaneMouseClicked
    
    /** Coordinates to centimeters */
    private static double coordToCM(int upLeftX, int downRightX, int width) 
        {
		//x line goes horizontally ...  y line goes vertically
		
		double xLineLength = downRightX - upLeftX;
		double oneUnitSize = xLineLength / width;			//size of one ground board
		double oneCmInCoords = oneUnitSize / 50;

		return oneCmInCoords;
	}
    
    /** Calculating required path from first point to second */
    private static int pathLength(int currX, int currY, int nextX, int nextY, double cmCoordUnit)
	{
		double pLngth;
		double lengthInCoord = Math.sqrt( Math.pow((nextX - currX), 2) + Math.pow((nextY - currY), 2) );
                System.err.println(lengthInCoord);
		pLngth = lengthInCoord / cmCoordUnit;
                return (int)(pLngth);
	}
    
    
    /** Function to determine heading    */
    private static int determineHeading(int currX, int currY, int nextX, int nextY)
	{
		double xLength = Math.abs(nextX - currX);	
		double yLength = Math.abs(nextY - currY);
		//double path = Math.sqrt( Math.pow((nextX - currX), 2) + Math.pow((nextY - currY), 2) );
		//triangle values complete
		double angle = Math.toDegrees(Math.atan(xLength/yLength));
		int heading = (int)angle;
		
		if( nextX > currX && nextY < currY)			//1-89
		{
			heading = 0 + heading;
		}
		else if(nextX > currX && nextY > currY)		//91-179
		{
			heading = 180 - heading;
		}
		else if(nextX < currX && nextY > currY)		//181-269
		{
			heading = 180 + heading;
		}
		else if(nextX < currX && nextY < currY)		//271-359
		{
			heading = 360 - heading;
		}
		else if(nextX == currX && nextY < currY)	// 0
		{
			heading = 0;
		}
		else if(nextX > currX && nextY == currY)	//90
		{
			heading = 90;
		}
		else if(nextX == currX && nextY > currY)	//180
		{
			heading = 180;
		}
		else if(nextX < currX && nextY == currY)	//270
		{
			heading = 270;
		}
		else		//coordinates are the same
		{
			//do nothing??
		}
		
		
		return heading;
	}    
    
    /** Determine degree turn */
    private void turn(int currH, int nextH) //currH = 235 nextH = 326   //L124
	{
		int turnLeft;
		int turnRight;

		
		if(currH < nextH)
		{
			turnLeft = currH + (360-nextH); //269
			turnRight = nextH - currH;      //91
		}
		else if(currH > nextH)
		{
			turnLeft = currH - nextH;
			turnRight = (360-currH) + nextH;
		}
		else
		{
			turnLeft = 0;
			turnRight = 0;
		}		
		
		if(turnLeft < turnRight)
		{
			System.out.println("Optimal turn is left.");
                        if (turnLeft != 0) {
                            addDataToTextArea("L"+turnLeft);
                        }
		}
		else
		{
			System.out.println("Optimal turn is right. ");
                        if (turnRight != 0) {
                            addDataToTextArea("R"+turnRight);
                        }
		}

	}
    
    
    private void saveConnection() {
        if (textChangeIP.getText().isEmpty() != true ) {
            connectionURL = textChangeIP.getText();
            textChangeIP.setText("");
            labCurrentIP2.setText(connectionURL);
        }
        if (textChangeName.getText().isEmpty() != true) {
            nameRobot = textChangeName.getText();
            textChangeName.setText("");
            labCurrentName2.setText(nameRobot);
        }
        if (textChangePass.getText().isEmpty() != true) {
            passRobot = textChangePass.getText();
            textChangePass.setText("");
            labCurrentPass2.setText(passRobot);
        }
    }
    
    private void sendRobotData() {
        //-------------------------------------------------------
        // PARAMETRI ZA POŠILJANJE
        // Spremenite vrednosti, da se ujemajo z vašim robotom
        //-------------------------------------------------------
        // Naslov streznika in pot do programa za posiljanje podatkov
        String url = "http://"+connectionURL+"/satNeXT/send.php";
        // Ime robota
        String robot = nameRobot;
        // Geslo za komunikacijo s satelitom satNeXT -
        // določili ste ga pri prijavi robota
        String password = passRobot;
        // Podatki, ki jih želimo poslati robotu.
        // Največja dovoljena dolžina je 50 bajtov.
        dataRobot = toSendText.getText();
        String data = dataRobot+"*";
        dataRobot = "";
        
        //---------------------------------------------------
        //POŠILJANJE ZAHTEVE NA STREŽNIK HTTP
        //---------------------------------------------------
        // Program send.php na strežniku pričakuje tri parametre:
        //   - ime robota: 'robot'
        //   - geslo: 'password'
        //   - podatki: 'data'
        // Klic tega programa iz spletnega brskalnika bi tako lahko bil tak:
        // <URL strežnika>/satNeXT/send.php?robot=Robotek&password=1234&data=Zivjo
        //
        // Ta klic moramo sedaj sestaviti sami:
        String urlParameters = "robot="+robot+"&password="+password+"&data="+data;

        // Ustvarimo nov objekt za posiljanje.

        // Posljemo podatke!
        System.out.println("Posiljanje zahteve na streznik");
        toSendText.setText("");
        coordinatesToDraw = "";
        labBackStatus.setText("");
        backPane.iLovePainting(coordinatesToDraw);
        System.err.println(firstX+" "+firstY);
        backPane.iAmSupposedToBeHere(firstX, firstY);
        mainCounter = 0;
        recvSendText.setText(recvSendText.getText()+"Posiljanje zahteve na streznik \nPodatki: "+data+"\n");
        try {
            sendHTTP(url, urlParameters);
        } catch (Exception e) {
            System.out.println(connectionURL);
        }
    }
    
    private void recieveRobotData() {
        //-------------------------------------------------------
        // PARAMETRI ZA POŠILJANJE
        // Spremenite vrednosti, da se ujemajo z vašim robotom
        //-------------------------------------------------------
        // Naslov streznika in pot do programa za posiljanje podatkov
        String url = "http://"+connectionURL+"/satNeXT/recv.php";
        // Ime robota
        String robot = nameRobot;
        // Geslo za komunikacijo s satelitom satNeXT -
        // določili ste ga pri prijavi robota
        String password = passRobot;

        //---------------------------------------------------
        // POŠILJANJE ZAHTEVE NA STREŽNIK HTTP
        //---------------------------------------------------
        // Program send.php na strežniku pričakuje tri parametre:
        //   - ime robota: 'robot'
        //   - geslo: 'password'
        // Klic tega programa iz spletnega brskalnika bi tako lahko bil tak:
        // <URL strežnika>/satNeXT/recv.php?robot=Robotek&password=1234
        //
        // Ta klic moramo sedaj sestaviti sami:
        String urlParameters = "robot="+robot+"&password="+password;

        // Ustvarimo nov objekt za posiljanje.

        // Posljemo podatke!
        System.out.println("Posiljanje zahteve na streznik ...");
        try {
            sendHTTPforResponse(url, urlParameters);
        } catch (Exception e) {
            System.out.println(connectionURL);
        }
    }
    
    
    //Adding commands that will be sent
    private void addDataToTextArea(String textAreaData) {
        if (!toSendText.getText().isEmpty()) {
            toSendText.setText(toSendText.getText()+"-");
        }
        toSendText.setText(toSendText.getText()+textAreaData);
        labBackStatus.setText(toSendText.getText()+"            Current heading is: "+deleteAllHeading+"   Calculated heading is: "+currentHeading);
    }
    
    private void addDataToCoordinates(String coord) {
        if (!coordinatesToDraw.equals("")) {
            coordinatesToDraw = coordinatesToDraw + "-";
        }
        coordinatesToDraw = coordinatesToDraw + coord;
    }
    

    
    
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Back;
    private javax.swing.JMenuItem Recieve;
    private javax.swing.JMenuItem about;
    private javax.swing.JMenuItem autoGrab;
    private projectroboliga2014.BackgroundPane backPane;
    private javax.swing.JMenuItem beep;
    private javax.swing.JMenuItem changeCon;
    private javax.swing.JPanel changeConPane;
    private javax.swing.JTextField changePlates;
    private javax.swing.JMenu commands;
    private javax.swing.JButton conClose;
    private javax.swing.JButton conSave;
    private javax.swing.JButton conSaveClose;
    private javax.swing.JMenu connection;
    private javax.swing.JFrame connectionFrame;
    private javax.swing.JLabel curPlates;
    private javax.swing.JPanel currentCon;
    private javax.swing.JMenuItem deleteAll;
    private javax.swing.JMenuItem deleteLast;
    private javax.swing.JMenuItem down;
    private javax.swing.JMenuItem downArm;
    private javax.swing.JMenu help;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labBackStatus;
    private javax.swing.JLabel labChange1;
    private javax.swing.JLabel labChange2;
    private javax.swing.JLabel labChangeIP1;
    private javax.swing.JLabel labChangeName1;
    private javax.swing.JLabel labChangePass1;
    private javax.swing.JLabel labChangePlates;
    private javax.swing.JLabel labConnectionFrame;
    private javax.swing.JLabel labCurrPlates;
    private javax.swing.JLabel labCurrentIP1;
    private javax.swing.JLabel labCurrentIP2;
    private javax.swing.JLabel labCurrentName1;
    private javax.swing.JLabel labCurrentName2;
    private javax.swing.JLabel labCurrentPass1;
    private javax.swing.JLabel labCurrentPass2;
    private javax.swing.JLabel labMapSettingsFrame;
    private javax.swing.JLabel labReset;
    private javax.swing.JLabel labSent;
    private javax.swing.JLabel labTextToSend;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenuItem mapSettings;
    private javax.swing.JButton mapSettingsClose;
    private javax.swing.JFrame mapSettingsFrame;
    private javax.swing.JButton mapSettingsReset;
    private javax.swing.JButton mapSettingsSave;
    private javax.swing.JButton mapSettingsSaveClose;
    private javax.swing.JMenu orders;
    private javax.swing.JTextArea recvSendText;
    private javax.swing.JMenuItem resetHeading;
    private javax.swing.JMenuItem send;
    private javax.swing.JFrame sendRecievedFrame;
    private javax.swing.JPanel sendRecievedPane;
    private javax.swing.JMenuItem sentRecieved;
    private javax.swing.JButton specClose;
    private javax.swing.JButton specRecv;
    private javax.swing.JButton specSend;
    private javax.swing.JTextField textChangeIP;
    private javax.swing.JTextField textChangeName;
    private javax.swing.JTextField textChangePass;
    private javax.swing.JTextArea toSendText;
    private javax.swing.JMenuItem turnRobot180;
    private javax.swing.JMenuItem up;
    // End of variables declaration//GEN-END:variables
}
