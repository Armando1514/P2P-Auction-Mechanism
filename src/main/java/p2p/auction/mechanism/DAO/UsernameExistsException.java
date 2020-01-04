package p2p.auction.mechanism.DAO;

class UsernameExistsException extends DAOException {

    UsernameExistsException(String message) {

        super(message);
    }
}