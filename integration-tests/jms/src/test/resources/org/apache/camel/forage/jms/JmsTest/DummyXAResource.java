package org.apache.camel.quarkus.messaging.jms;

import org.jboss.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource {
    private static final Logger LOG = Logger.getLogger(DummyXAResource.class);

    public DummyXAResource() {
    }

    public void commit(Xid xid, boolean b) throws XAException {
        LOG.info("DummyXAResource commit " + String.valueOf(xid));
    }

    public void end(Xid xid, int i) throws XAException {
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        return !(xaResource instanceof DummyXAResource) ? false : this.equals(xaResource);
    }

    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    public Xid[] recover(int i) throws XAException {
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        LOG.info("DummyXAResource rollback " + String.valueOf(xid));
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        return true;
    }

    public void start(Xid xid, int i) throws XAException {
    }
}