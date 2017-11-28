package leelawatcher.goboard;

public class IllegalMoveException extends Exception {
  private PointOfPlay proposedMove;
  private Position position;

  IllegalMoveException(PointOfPlay proposedMove, Position position) {
    super("Illegal move:" + proposedMove + "\nPosition:\n" + position);
    this.proposedMove = proposedMove;
    this.position = position;
  }

  public PointOfPlay getProposedMove() {
    return proposedMove;
  }

  public Position getPosition() {
    return position;
  }
}
