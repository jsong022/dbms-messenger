/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("\n---------------");
            System.out.println("  LOG-IN MENU");
            System.out.println("---------------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("\n---------------");
                System.out.println("   MAIN MENU");
                System.out.println("---------------");
                System.out.println("1. View/Manage contact list");
                System.out.println("2. View/Manage Blocked contacts");
                System.out.println("3. Browse Chats");
                System.out.println("4. Update Status");
                System.out.println("5. Create a new Chat");
                System.out.println(".........................");
                System.out.println("8. Delete account");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: ListContacts(esql, authorisedUser); break;
                   case 2: BlockContacts(esql, authorisedUser); break;
                   case 3: ListChats(esql, authorisedUser); break;
                   case 4: UpdateStatus(esql, authorisedUser); break;
                   case 5: CreateChat(esql, authorisedUser); break;
                   case 8: DropUser(esql, authorisedUser);
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
	System.out.println("**************************************************");
	System.out.println("  WELCOME TO BRETT AND JAY'S MESSENGER PROGRAM!!");
	System.out.println("**************************************************");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

   //Creating empty contact\block lists for a user
   esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
   int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
   int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
         
   String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
    public static void CreateChat(Messenger esql, String user){
        try{
            int count = 0;
            List<String> myList = new ArrayList<String>();
            boolean cont = true; 
            System.out.println("Enter the login of the user you wish to add to chat.");
            System.out.println("After the final member of the chat, enter an empty login.");
            System.out.print("\tlogin to add to new chat: ");
            String login = in.readLine();
            myList.add(login);
            count += 1;
            while (cont)
            {
                System.out.print("\tlogin to add to new chat: ");
                login = in.readLine();
                if (login.length() == 0)
                {
                    cont = false;
                }
                else
                {
                    myList.add(login);
                    count += 1;
                }
            }
            String chat_type = "private";
            if (count > 1){
                chat_type = "group";}
                
            //Creating empty chat for a user
            String query = String.format("INSERT INTO chat(chat_type, init_sender) " +
            "VALUES ('%s', '%s');", chat_type, user);
            esql.executeUpdate(query);
            int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
            query = String.format("INSERT INTO chat_list (chat_id, member) " +
            "VALUES (%d, '%s'); ", chat_id, user);
            esql.executeUpdate(query);
            for (int i = 0; i < count; i++)
            {
                query = String.format("INSERT INTO chat_list (chat_id, member) " +
                "VALUES (%d, '%s'); ", chat_id, myList.get(i));
                esql.executeUpdate(query);
            }
            String chatID = String.format("%d", chat_id);
            PostMessage(esql, user, chatID);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

   public static void DropUser(Messenger esql, String user){
      try{
       //Drop user
       esql.executeUpdate(String.format("DELETE FROM USR WHERE login = '%s';", user));
         System.out.println ("User successfully deleted!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s';", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void AddToBlock(Messenger esql, String user){
      try{
      	System.out.print("\tEnter login to add to Block List: ");
	String newContact = in.readLine();
    if (newContact == user){
        System.out.println("Cannot add self to Block List");
        return;
    }
	String query = String.format("INSERT INTO USER_LIST_CONTAINS (list_id, list_member)" +
        "(SELECT u1.block_list AS list_id, u2.login AS list_member " +
        "FROM usr u1, usr u2 " +
        "WHERE u1.login = '%s' AND u2.login = '%s');", user, newContact);
	esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void RemoveFromBlock(Messenger esql, String user){
      try{
      	System.out.print("\tEnter login to remove from Block List: ");
	String newContact = in.readLine();
	String query = String.format("DELETE FROM user_list_contains " +
        "WHERE list_id IN " +
            "(SELECT block_list AS list_id FROM usr WHERE login = '%s') " +
            "AND list_member = '%s';", user, newContact);
	esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void AddToContact(Messenger esql, String user){
      try{
      	System.out.print("\tEnter login to add to Contact List: ");
	String newContact = in.readLine();
    if (newContact == user){
        System.out.println("Cannot add self to Contact List");
        return;
    }
	String query = String.format("INSERT INTO USER_LIST_CONTAINS (list_id, list_member)" +
        "(SELECT u1.contact_list AS list_id, u2.login AS list_member " +
        "FROM usr u1, usr u2 " +
        "WHERE u1.login = '%s' AND u2.login = '%s');", user, newContact);
	esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void RemoveFromContact(Messenger esql, String user){
      try{
      	System.out.print("\tEnter login to remove from Contact List: ");
	String newContact = in.readLine();
	String query = String.format("DELETE FROM user_list_contains " +
        "WHERE list_id IN " +
            "(SELECT contact_list AS list_id FROM usr WHERE login = '%s') " +
            "AND list_member = '%s';", user, newContact);
	esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void ManageContactList(Messenger esql, String user){
    System.out.println("\n--------------------------------------------------");
   	System.out.println(" How would you like to change your Contact List?");
    System.out.println("--------------------------------------------------");
	System.out.println("1. Add to contact list");
	System.out.println("2. Remove from contact list");
    System.out.println("3. Return to Main Menu");
	switch(readChoice()){
		case 1: AddToContact(esql,user); break;
		case 2: RemoveFromContact(esql,user); break;
        case 3: System.out.println("Returning to Main Menu"); break;
		default: System.out.println("Invalid Choice! Returning to Main Menu"); break;
	}
   }//end

   public static void ManageBlockList(Messenger esql, String user){
    System.out.println("\n--------------------------------------------------");
   	System.out.println(" How would you like to change your Block List?");
    System.out.println("--------------------------------------------------");
	System.out.println("1. Add to block list");
	System.out.println("2. Remove from block list");
    System.out.println("3. Return to Main Menu");
	switch(readChoice()){
		case 1: AddToBlock(esql,user); break;
		case 2: RemoveFromBlock(esql,user); break;
        case 3: System.out.println("Returning to Main Menu"); break;
		default: System.out.println("Invalid Choice! Returning to Main Menu"); break;
	}

   }//end


   public static void ListContacts(Messenger esql, String user){
      try{
        String query = String.format(" SELECT login, status " +
        " FROM usr " +
        " WHERE login IN ( SELECT list_member " +
                         " FROM user_list_contains ulc " +
                         " WHERE ulc.list_id IN(SELECT contact_list " +
					      " FROM usr " +
					      " WHERE login = '%s')); ", user);
        
/*        String query = String.format("select login, status " +
                      "from USR where login " +
                      "in ( " +
                          "select list_member " +
                          "from  user_list_contains as ulc, " + 
                                "user_list as ul, " +
                                "usr as u " +
                          "where ulc.list_id = ul.list_id " +
                            "and ul.list_type = 'contact' " +
                            "and u.contact_list = ul.list_id " +
                            "and login = '%s' " +
                          ")"+
                      ";", user);*/
        System.out.println("\nContact List:");
        esql.executeQueryAndPrintResult(query);
        ManageContactList(esql, user);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void BlockContacts(Messenger esql, String user){
      try{
        String query = String.format("SELECT login, status " +
        " FROM usr " +
        " WHERE login IN ( SELECT list_member " +
                         " FROM user_list_contains ulc " +
                         " WHERE ulc.list_id IN(SELECT block_list " +
					      " FROM usr " +
					      " WHERE login = '%s')); " , user);
/*        String query = String.format("select login " +
                      "from USR where login " +
                      "in ( " +
                          "select list_member " +
                          "from  user_list_contains as ulc, " + 
                                "user_list as ul, " +
                                "usr as u " +
                          "where ulc.list_id = ul.list_id " +
                            "and ul.list_type = 'block' " +
                            "and u.block_list = ul.list_id " +
                            "and login = '%s' " +
                          ")"+
                      ";", user);*/
        System.out.println("\nBlocked Contacts:");
        esql.executeQueryAndPrintResult(query);
        ManageBlockList(esql, user);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

    public static void PostMessage(Messenger esql, String user, String chatID){
        try{
            System.out.print("\tEnter message: ");
            String msg_text = in.readLine();
            if (msg_text.length() > 300){
                System.out.println("Message cannot be longer than 300 characters!");
                return;
            }
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp ts = new java.sql.Timestamp(calendar.getTime().getTime());
            String msg_timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
            String query = String.format("INSERT INTO message (msg_text, msg_timestamp, sender_login, chat_id) " +
            "VALUES ('%s', '%s', '%s', %s);", msg_text, msg_timestamp, user, chatID);
            esql.executeUpdate(query);
            ViewChatMessages(esql,user,chatID,0);
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void EditSingleMessage(Messenger esql, String user, String chatID){
        try{
            System.out.print("\tEnter msg_id to Edit: ");
            String msgID = in.readLine();
            System.out.print("\tEnter new message text: ");
            String msgText = in.readLine();
            if (msgText.length() > 300)
            {
				System.out.println("Message text cannot be longer than 300 characters!");
				return;
			}
            String query = String.format("UPDATE message " +
            "SET msg_text = '%s' " +
            "WHERE msg_id = %s AND chat_id = %s;", msgText, msgID, chatID);
            esql.executeUpdate(query);
            EditMessages(esql, user, chatID, 0);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end
    
	public static void EditMessages(Messenger esql, String user, String chatID, int offset)
    {
        try
        {
            String query = String.format("SELECT m.msg_id, m.sender_login, m.msg_timestamp, m.msg_text " +
            "FROM message m " +
            "WHERE m.chat_id = '%s' AND m.sender_login = '%s' " +
            "ORDER BY msg_timestamp DESC " + 
            "LIMIT 10 OFFSET '%s' ", chatID, user, offset);
            String temp = String.format("\nEditable Messages from chatID: %s ", chatID);
            System.out.println(temp);
            esql.executeQueryAndPrintResult(query);
            System.out.println("\n----------------------------------");
            System.out.println(" What would you like to do?");
            System.out.println("----------------------------------");
            if (offset >= 10 )
            {
                String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
                String c2 = String.format("2. Load previous page (current page = %d)", (offset/10)+1);
                System.out.println(c1);
                System.out.println(c2);
                System.out.println("3. Edit a single message");
                System.out.println("4. Return to Main Menu");
                switch(readChoice())
                {
                    case 1: EditMessages(esql, user, chatID, offset+10);
                        break;
                    case 2: EditMessages(esql, user, chatID, offset-10);
                        break;
                    case 3: EditSingleMessage(esql, user, chatID);
                        break;
                    case 4: System.out.println("Returning to Main Menu");
                        break;
                    default: System.out.println("Invalid Choice! Returning to Main Menu");
                        break;
                }
            }
            else
            {
                String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
                System.out.println(c1);
                System.out.println("2. Edit a single message");
                System.out.println("3. Return to Main Menu");
                switch(readChoice())
                {
                    case 1: EditMessages(esql, user, chatID, offset+10);
                        break;
                    case 2: EditSingleMessage(esql, user, chatID);
                        break;
                    case 3: System.out.println("Returning to Main Menu");
                        break;
                    default: System.out.println("Invalid Choice! Returning to Main Menu");
                        break;
                }
            }
        } catch(Exception e)
        {
            System.err.println (e.getMessage());
        }
    }
    
    public static void DeleteChat(Messenger esql, String chatID)
    {   
		try{
            String query = String.format("DELETE FROM chat WHERE chat_id = %s", chatID);
			esql.executeUpdate(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
		
	}
    
    public static void DeleteSingleMessage(Messenger esql, String user, String chatID){
        try{
            System.out.print("\tEnter msg_id to Delete: ");
            String msgID = in.readLine();
            String query = String.format("DELETE FROM message " +
            "WHERE msg_id = %s AND chat_id = %s;", msgID, chatID);
            esql.executeUpdate(query);
            DeleteMessages(esql, user, chatID, 0);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end
   
    public static void DeleteMessages(Messenger esql, String user, String chatID, int offset)
    {
        try
        {
			String loginquery = String.format("SELECT init_sender FROM chat WHERE chat_id = '%s';", chatID);
			List<List<String>>myList = esql.executeQueryAndReturnResult(loginquery);
			String chat_owner = myList.get(0).get(0);
			chat_owner = chat_owner.replace(" ","");
			if (user.compareTo(chat_owner) == 0)
			{   
				String query = String.format("SELECT m.msg_id, m.sender_login, m.msg_timestamp, m.msg_text " +
	            "FROM message m, chat c " +
	            "WHERE m.chat_id = c.chat_id AND m.chat_id = '%s' AND (m.sender_login = '%s' OR c.init_sender = '%s') " +
	            "ORDER BY msg_timestamp DESC " + 
	            "LIMIT 10 OFFSET '%s' ", chatID, user, user, offset);
	            String temp = String.format("\nDeletable Messages from chatID: %s ", chatID);
	            System.out.println(temp);
	            esql.executeQueryAndPrintResult(query);
	            System.out.println("\n----------------------------------");
	            System.out.println(" What would you like to do?");
	            System.out.println("----------------------------------");
	            if (offset >= 10 )
	            {
	                String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
	                String c2 = String.format("2. Load previous page (current page = %d)", (offset/10)+1);
	                System.out.println(c1);
	                System.out.println(c2);
	                System.out.println("3. Delete a single message");
	                System.out.println("4. Delete entire chat");
	                System.out.println("5. Return to Main Menu");
	                switch(readChoice())
	                {
	                    case 1: DeleteMessages(esql, user, chatID, offset+10);
	                        break;
	                    case 2: DeleteMessages(esql, user, chatID, offset-10);
	                        break;
	                    case 3: DeleteSingleMessage(esql, user, chatID);
	                        break;
	                    case 4: DeleteChat(esql, chatID);
	                        break;
	                    case 5: System.out.println("Returning to Main Menu");
	                        break;
	                    default: System.out.println("Invalid Choice! Returning to Main Menu");
	                        break;
	                }
	            }
	            else
	            {
	                String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
	                System.out.println(c1);
	                System.out.println("2. Delete a single message");
	                System.out.println("3. Delete entire chat");
	                System.out.println("4. Return to Main Menu");
	                switch(readChoice())
	                {
	                    case 1: DeleteMessages(esql, user, chatID, offset+10);
	                        break;
	                    case 2: DeleteSingleMessage(esql, user, chatID);
	                        break;
	                    case 3: DeleteChat(esql, chatID);
	                        break;
	                    case 4: System.out.println("Returning to Main Menu");
	                        break;
	                    default: System.out.println("Invalid Choice! Returning to Main Menu");
	                        break;
	                }
	            }
			}
			else
			{
				String query = String.format("SELECT msg_id, sender_login, msg_timestamp, msg_text " +
	            "FROM message " +
	            "WHERE chat_id = '%s' AND sender_login = '%s' " +
	            "ORDER BY msg_timestamp DESC " + 
	            "LIMIT 10 OFFSET '%s' ", chatID, user, offset);
	            String temp = String.format("\nDeletable Messages from chatID: %s ", chatID);
	            System.out.println(temp);
	            esql.executeQueryAndPrintResult(query);
	            System.out.println("\n----------------------------------");
	            System.out.println(" What would you like to do?");
	            System.out.println("----------------------------------");
	            if (offset >= 10 )
	            {
	                String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
	                String c2 = String.format("2. Load previous page (current page = %d)", (offset/10)+1);
	                System.out.println(c1);
	                System.out.println(c2);
	                System.out.println("3. Delete a single message");
	                System.out.println("4. Return to Main Menu");
	                switch(readChoice())
	                {
	                    case 1: DeleteMessages(esql, user, chatID, offset+10);
	                        break;
	                    case 2: DeleteMessages(esql, user, chatID, offset-10);
	                        break;
	                    case 3: DeleteSingleMessage(esql, user, chatID);
	                        break;
	                    case 4: System.out.println("Returning to Main Menu");
	                        break;
	                    default: System.out.println("Invalid Choice! Returning to Main Menu");
	                        break;
	                }
	            }
	            else
	            {
	                String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
	                System.out.println(c1);
	                System.out.println("2. Delete a single message");
	                System.out.println("3. Return to Main Menu");
	                switch(readChoice())
	                {
	                    case 1: DeleteMessages(esql, user, chatID, offset+10);
	                        break;
	                    case 2: DeleteSingleMessage(esql, user, chatID);
	                        break;
	                    case 3: System.out.println("Returning to Main Menu");
	                        break;
	                    default: System.out.println("Invalid Choice! Returning to Main Menu");
	                        break;
	                }
	            }
			}
        } catch(Exception e)
        {
            System.err.println (e.getMessage());
        }
    }
    public static void ViewChatMessages(Messenger esql, String user, String chatID, int offset){
        try{
            String query = String.format("SELECT msg_id, sender_login," +
            " msg_timestamp, msg_text FROM message " +
            "WHERE chat_id = '%s' AND sender_login NOT IN (SELECT ulc.list_member AS sender_login " +
                                                          "FROM user_list_contains ulc, usr u " +
                                                          "WHERE ulc.list_id = u.block_list AND u.login = '%s') " +
            "ORDER BY msg_timestamp DESC " + 
            "LIMIT 10 OFFSET '%s'", chatID, user, offset);
            String temp = String.format("\nchatID: %s", chatID);
            System.out.println(temp);
            esql.executeQueryAndPrintResult(query);
            System.out.println("\n----------------------------------");
            System.out.println(" What would you like to do?");
            System.out.println("----------------------------------");
            if (offset >= 10 ){
            	String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
				String c2 = String.format("2. Load previous page (current page = %d)", (offset/10)+1);
				System.out.println(c1);
				System.out.println(c2);
				System.out.println("3. Post a new message");
				System.out.println("4. Delete messages");
				System.out.println("5. Edit messages");
            	System.out.println("6. Return to Main Menu");
            	switch(readChoice()){
                    case 1: ViewChatMessages(esql, user, chatID, offset+10);
                    	break;
				    case 2: ViewChatMessages(esql, user, chatID, offset-10);
						break;
				    case 3: PostMessage(esql,user,chatID);
						break;
				    case 4: DeleteMessages(esql, user, chatID, 0);
						break;
					case 5: EditMessages(esql, user, chatID, 0);
						break;
                    case 6: System.out.println("Returning to Main Menu");
                    	break;
                    default: System.out.println("Invalid Choice! Returning to Main Menu");
						break;
            	}
	    }
            else{
            	String c1 = String.format("1. Load next page (current page = %d)", (offset/10)+1);
				System.out.println(c1);
				System.out.println("2. Post a new message");
				System.out.println("3. Delete messages");
				System.out.println("4. Edit messages");
            	System.out.println("5. Return to Main Menu");
            	switch(readChoice()){
                    case 1: ViewChatMessages(esql, user, chatID, offset+10);
                    	break;
		    case 2: PostMessage(esql,user,chatID);
			break;
		    case 3: DeleteMessages(esql, user, chatID, 0);
                break;
					case 4: EditMessages(esql, user, chatID, 0);
						break;
                    case 5: System.out.println("Returning to Main Menu");
                    	break;
                    default: System.out.println("Invalid Choice! Returning to Main Menu");
                   	break;
            	}
	    }
        } catch(Exception e){
            System.err.println (e.getMessage());
        }
    }//end

    public static void ViewChat(Messenger esql, String user){
        try{
            System.out.print("\tEnter the chat_id to view:");
            String chatID = in.readLine();
            ViewChatMessages(esql, user, chatID, 0);
        } catch(Exception e){
            System.err.println (e.getMessage());
        }
    }//end
    
    public static void AddMembersToChat(Messenger esql, String user, String chatID)
    {
		try
		{
			System.out.print("\tEnter login to add to Chat: ");
			String newContact = in.readLine();
			String query = String.format("INSERT INTO chat_list (chat_id, member) " +
			"VALUES (%s, '%s')", chatID, newContact);
			esql.executeUpdate(query);
		}catch(Exception e)
		{
			System.err.println (e.getMessage ());
		}
	}    
	
    public static void RemoveMembersFromChat(Messenger esql, String user, String chatID)
    {
		try
		{
			System.out.print("\tEnter login to remove from Chat: ");
			String newContact = in.readLine();
			String query = String.format("DELETE FROM chat_list " +
			"WHERE chat_id = %s AND member = '%s';", chatID, newContact);
			esql.executeUpdate(query);
		}catch(Exception e)
		{
			System.err.println (e.getMessage ());
		}
	}
	
    public static void EditChat(Messenger esql, String user){
        try{
			String chatID;
			String query = String.format ("SELECT chat_id FROM chat WHERE init_sender = '%s';", user);
			System.out.println("\nChats you own:");
			esql.executeQueryAndPrintResult(query);
            System.out.println("\n----------------------------------");
            System.out.println("  What would you like to do?");
            System.out.println("----------------------------------");
            System.out.println("1. Add users to a chat");
            System.out.println("2. Remove users from a chat");
            System.out.println("3. Return to Main Menu");
            switch(readChoice())
            {
				case 1: System.out.print("\tEnter chat_id to Add members to: ");
					chatID = in.readLine();
					AddMembersToChat(esql, user, chatID);
					break;
				case 2: System.out.print("\tEnter chat_id to Remove members from: ");
					chatID = in.readLine();
					RemoveMembersFromChat(esql, user, chatID);
					break;
				case 3: System.out.println("Returning to Main Menu");
					break;
				default: System.out.println("Invalid Choice! Returning to Main Menu");
					break;
			}
        } catch(Exception e){
            System.err.println (e.getMessage());
        }
    }//end
    
   public static void ListChats(Messenger esql, String user){
   try{
        String query = String.format("SELECT * FROM " +
		"CHAT_lIST WHERE chat_id = ANY (SELECT chat_id FROM CHAT_LIST " +
		"WHERE member = '%s')",user);
        System.out.println("\nChat List:");
        esql.executeQueryAndPrintResult(query);
	}
	catch(Exception e){
        	System.err.println (e.getMessage ());
     	}
        System.out.println("\n---------------------------------------");
        System.out.println("  Would you like to view a Chat?");
        System.out.println("---------------------------------------");
      	System.out.println("1. View a chat");
      	System.out.println("2. Add/Remove users from a chat");
      	System.out.println("3. Return to Main Menu");
      	switch(readChoice()){
		case 1: ViewChat(esql, user); break;
		case 2: EditChat(esql, user); break;
		case 3: System.out.println("Returning to Main Menu") ; break;
		default: System.out.println("incorrect input"); break;
      	}
   }//end

   public static void UpdateStatus(Messenger esql, String user){
        try{
			String statusquery = String.format("SELECT status FROM usr WHERE login = '%s';", user);
			List<List<String>> myList = esql.executeQueryAndReturnResult(statusquery);
			String status = String.format("Current Status: " + myList.get(0).get(0));
			System.out.println("\n----------------------------------------");
			System.out.println(status);
			System.out.println("\n----------------------------------------");
            System.out.print("\tEnter new Status text: ");
            String msgText = in.readLine();
            if (msgText.length() > 140)
            {
				System.out.println("Status text cannot be longer than 140 characters!");
				return;
			}
            String query = String.format("UPDATE usr " +
            "SET status = '%s' " +
            "WHERE login = '%s'", msgText, user);
            esql.executeUpdate(query);
            myList = esql.executeQueryAndReturnResult(statusquery);
			status = String.format("New Status: " + myList.get(0).get(0));
			System.out.println("\n----------------------------------------");
			System.out.println(status);
			System.out.println("\n----------------------------------------");
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
   }//end 
}//end Messenger
