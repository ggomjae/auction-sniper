package auctionsniper;

public class AuctionSniper implements AuctionEventListener {

  private final Auction auction;
  private final SniperListener sniperListener;

  public AuctionSniper(Auction auction, SniperListener sniperListener) {
    this.auction = auction;
    this.sniperListener = sniperListener;
  }

  @Override
  public void currentPrice(int price, int increment) {
    auction.bid(price + increment);
    sniperListener.sniperBidding();
  }

  @Override
  public void auctionClosed() {
    sniperListener.sniperLost();
  }
}
