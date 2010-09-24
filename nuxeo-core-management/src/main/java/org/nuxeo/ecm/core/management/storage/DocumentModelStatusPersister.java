/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     mcedica
 */
package org.nuxeo.ecm.core.management.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.management.api.AdministrativeStatus;

/**
 * Used to control the server administrative status: the status of the server
 * can be passive or active.
 *
 * @author Mariana Cedica
 */

public class DocumentModelStatusPersister implements
        AdministrativeStatusPersister {

    public static final String ADMINISTRATIVE_INFO_CONTAINER = "administrative-infos";

    public static final String ADMINISTRATIVE_INFO_CONTAINER_DOCUMENT_TYPE = "AdministrativeStatusContainer";

    public static final String ADMINISTRATIVE_STATUS_DOCUMENT_TYPE = "AdministrativeStatus";

    public static final String STATUS_PROPERTY = "status:administrative_status";

    public static final String MESSAGE_PROPERTY = "status:statusMessage";

    public static final String INSTANCE_PROPERTY = "status:instanceId";

    public static final String SERVICE_PROPERTY = "status:serviceId";

    public static final String LOGIN_PROPERTY = "status:userLogin";

    private static final Log log = LogFactory.getLog(DocumentModelStatusPersister.class);

    private class StatusSaver extends DocumentStoreSessionRunner {

        protected AdministrativeStatus status;

        public StatusSaver(AdministrativeStatus status) {
            this.status = status;
        }

        @Override
        protected String errorMessage() {
            return "Cannot save  " + status;
        }

        @Override
        public void run() throws ClientException {
            doGetOrCreateDoc(status);
            session.save();
        }

        public AdministrativeStatus getStatus() {
            return status;
        }

        protected DocumentModel doGetOrCreateContainer() throws ClientException {

            DocumentRef admRootDocRef = DocumentStoreManager.newPath(ADMINISTRATIVE_INFO_CONTAINER);

            if (!session.exists(admRootDocRef)) {
                DocumentModel doc = session.createDocumentModel(DocumentStoreManager.newPath().toString(),
                        ADMINISTRATIVE_INFO_CONTAINER,
                        ADMINISTRATIVE_INFO_CONTAINER_DOCUMENT_TYPE);
                doc.setPropertyValue("dc:title", ADMINISTRATIVE_INFO_CONTAINER);
                doc = session.createDocument(doc);
                session.save();
            }

            return session.getDocument(admRootDocRef);
        }

        protected DocumentModel doGetOrCreateDoc(AdministrativeStatus status)
                throws ClientException {
            DocumentModel administrativeContainer = doGetOrCreateContainer();

            DocumentRef statusDocRef = new PathRef(
                    administrativeContainer.getPathAsString() + "/"
                            + getAdministrativeStatusDocName(status));

            DocumentModel doc = null;
            boolean create = false;
            if (!session.exists(statusDocRef)) {
                create = true;
                doc = session.createDocumentModel(
                        administrativeContainer.getPathAsString(),
                        getAdministrativeStatusDocName(status),
                        ADMINISTRATIVE_STATUS_DOCUMENT_TYPE);
            } else {
                doc = session.getDocument(statusDocRef);
            }

            doc.setPropertyValue(DocumentModelStatusPersister.LOGIN_PROPERTY,
                    status.getUserLogin());
            doc.setPropertyValue(
                    DocumentModelStatusPersister.INSTANCE_PROPERTY,
                    status.getInstanceIdentifier());
            doc.setPropertyValue(DocumentModelStatusPersister.SERVICE_PROPERTY,
                    status.getServiceIdentifier());
            doc.setPropertyValue(DocumentModelStatusPersister.MESSAGE_PROPERTY,
                    status.getMessage());
            doc.setPropertyValue(DocumentModelStatusPersister.STATUS_PROPERTY,
                    status.getState());

            doc.setPropertyValue("dc:title",
                    getAdministrativeStatusDocName(status));

            if (create) {
                doc = session.createDocument(doc);
            } else {
                doc = session.saveDocument(doc);
            }
            session.save();

            return doc;
        }

    }

    protected String getAdministrativeStatusDocName(AdministrativeStatus status) {
        return status.getInstanceIdentifier() + "--"
                + status.getServiceIdentifier();
    }

    public class StatusFetcher extends DocumentStoreSessionRunner {

        protected String instanceId;

        protected String serviceId;

        protected List<String> allInstanceIds = new ArrayList<String>();

        protected List<AdministrativeStatus> statuses = new ArrayList<AdministrativeStatus>();

        public StatusFetcher(String instanceId, String serviceId) {
            this.instanceId = instanceId;
            this.serviceId = serviceId;
        }

        @Override
        protected String errorMessage() {
            StringBuffer sb = new StringBuffer();
            sb.append("Cannot fetch statuses ");
            if (instanceId != null) {
                sb.append(" for ").append(instanceId);
            }
            if (serviceId != null) {
                sb.append(":").append(serviceId);
            }
            return sb.toString();
        }

        @Override
        public void run() throws ClientException {

            boolean onlyFetchIds = false;

            StringBuffer sb = new StringBuffer("select * from ");
            sb.append(DocumentModelStatusPersister.ADMINISTRATIVE_STATUS_DOCUMENT_TYPE);

            if (instanceId == null) {
                onlyFetchIds = true;
            } else {
                sb.append(" where ");
                sb.append(DocumentModelStatusPersister.INSTANCE_PROPERTY);
                sb.append("='");
                sb.append(instanceId);
                sb.append("'");
                if (serviceId != null) {
                    sb.append(" AND ");
                    sb.append(DocumentModelStatusPersister.SERVICE_PROPERTY);
                    sb.append("='");
                    sb.append(serviceId);
                    sb.append("'");
                }
            }

            DocumentModelList result = session.query(sb.toString());

            for (DocumentModel doc : result) {
                if (onlyFetchIds) {
                    String id = (String) doc.getPropertyValue(DocumentModelStatusPersister.INSTANCE_PROPERTY);
                    if (!allInstanceIds.contains(id)) {
                        allInstanceIds.add(id);
                    }
                } else {
                    statuses.add(wrap(doc));
                }
            }
        }

        protected AdministrativeStatus wrap(DocumentModel doc)
                throws ClientException {

            String userLogin = (String) doc.getPropertyValue(DocumentModelStatusPersister.LOGIN_PROPERTY);
            String id = (String) doc.getPropertyValue(DocumentModelStatusPersister.INSTANCE_PROPERTY);
            String service = (String) doc.getPropertyValue(DocumentModelStatusPersister.SERVICE_PROPERTY);
            String message = (String) doc.getPropertyValue(DocumentModelStatusPersister.MESSAGE_PROPERTY);
            String state = (String) doc.getPropertyValue(DocumentModelStatusPersister.STATUS_PROPERTY);
            Calendar modified = (Calendar) doc.getPropertyValue("dc:modified");

            AdministrativeStatus status = new AdministrativeStatus(state,
                    message, modified, userLogin, id, service);

            return status;
        }
    }

    @Override
    public List<String> getAllInstanceIds() {
        StatusFetcher fetcher = new StatusFetcher(null, null);
        try {
            fetcher.runUnrestricted();
            return fetcher.allInstanceIds;
        } catch (ClientException e) {
            log.error("Error while fetching all instance Ids", e);
            return null;
        }
    }

    @Override
    public List<AdministrativeStatus> getAllStatuses(String instanceId) {
        StatusFetcher fetcher = new StatusFetcher(instanceId, null);
        try {
            fetcher.runUnrestricted();
            return fetcher.statuses;
        } catch (ClientException e) {
            log.error("Error while fetching all service status for instance "
                    + instanceId, e);
            return null;
        }
    }

    @Override
    public AdministrativeStatus getStatus(String instanceId,
            String serviceIdentifier) {
        StatusFetcher fetcher = new StatusFetcher(instanceId, serviceIdentifier);
        try {
            fetcher.runUnrestricted();
            if (fetcher.statuses.size() == 1) {
                return fetcher.statuses.get(0);
            } else {
                log.warn("Unable to fetch status for service "
                        + serviceIdentifier + " in instance " + instanceId);
                return null;
            }
        } catch (ClientException e) {
            log.error("Error while fetching all service status for instance "
                    + instanceId, e);
            return null;
        }
    }

    @Override
    public void remove(String instanceId) {
        throw new UnsupportedOperationException("Not implemented for now");
    }

    @Override
    public AdministrativeStatus saveStatus(AdministrativeStatus status) {

        try {
            StatusSaver saver = new StatusSaver(status);
            saver.runUnrestricted();
            return saver.getStatus();
        } catch (Exception e) {
            log.error("Error while saving status", e);
            return null;
        }

    }

}
