package auctionsniper;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import auctionsniper.ui.MainWindow;

public class Main implements SniperListener {

  private static final int ARG_HOSTNAME = 0;
  private static final int ARG_USERNAME = 1;
  private static final int ARG_PASSWORD = 2;
  private static final int ARG_ITEM_ID = 3;

  public static final String AUCTION_RESOURCE = "Auction";
  public static final String ITEM_ID_AS_LOGIN = "auction-%s";
  public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

  public static final String JOIN_COMMAND_FORMAT = "";
  public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID, Price: %d;";

  public static final String MAIN_WINDOW_NAME = "Auction Sniper";

  private MainWindow ui;
  @SuppressWarnings("unused")
  private Chat notToBeGCd;

  public Main() throws InvocationTargetException, InterruptedException {
    startUserInterface();
  }

  public static void main(String... args) throws InvocationTargetException, InterruptedException, XMPPException {
    Main main = new Main();
    main.joinAuction(
        connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]),
        args[ARG_ITEM_ID]);
  }

  @Override
  public void sniperLost() {
    SwingUtilities.invokeLater(() -> ui.showStatus(MainWindow.STATUS_LOST));
  }

  @Override
  public void sniperBidding() {
    SwingUtilities.invokeLater(() -> ui.showStatus(MainWindow.STATUS_BIDDING));
  }

  private void joinAuction(XMPPConnection connection, String itemId) throws XMPPException {
    disconnectWhenUICloses(connection);
    Chat chat = connection.getChatManager().createChat(auctionId(itemId, connection), null);

    this.notToBeGCd = chat;

    chat.addMessageListener(new AuctionMessageTranslator(new AuctionSniper(amount -> {
      try {
        chat.sendMessage(String.format(BID_COMMAND_FORMAT, amount));
      } catch (XMPPException e) {
        e.printStackTrace();
      }
    }, this)));
    chat.sendMessage(JOIN_COMMAND_FORMAT);
  }

  private void disconnectWhenUICloses(final XMPPConnection connection) {
    ui.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        connection.disconnect();
      }
    });
  }

  private static XMPPConnection connection(String hostname, String username, String password) throws XMPPException {
    XMPPConnection connection = new XMPPConnection(hostname);
    connection.connect();
    connection.login(username, password, AUCTION_RESOURCE);

    return connection;
  }

  private static String auctionId(String itemId, XMPPConnection connection) {
    return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
  }

  private void startUserInterface() throws InvocationTargetException, InterruptedException {
    SwingUtilities.invokeAndWait(() -> ui = new MainWindow());
  }
}
