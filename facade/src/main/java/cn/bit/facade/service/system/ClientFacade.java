package cn.bit.facade.service.system;

import cn.bit.facade.model.system.Client;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface ClientFacade {

    Client addClient(Client client);

    Client deleteClient(ObjectId id);

    Client getClientById(ObjectId id);

    Page<Client> getClients(int page, int size);

    List<Client> getClientByTypes(List<Integer> types, Integer partner);

}
