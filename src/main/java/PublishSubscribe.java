public interface PublishSubscribe {

    public boolean createTopic(String _topic_name);
    public boolean subscribeTopic(String _topic_name);
    public boolean publishToTopic(String _topic_name, Object _obj);
    public boolean unsubscribeFromTopic(String _topic_name);
    public boolean leaveNetwork();

}
