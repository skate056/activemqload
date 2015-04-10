activemqload
============
An load test which enqueues and dequeues 1 million dummy messages from an ActiveMQ queue. The message sender tags every 3rd message as bad message and the message handler throws an exception on every message which is tagged to fail. In short the processing of every 3rd message fails in the message handler. This test was specifically designed to expose a bug in ActiveMQ.
