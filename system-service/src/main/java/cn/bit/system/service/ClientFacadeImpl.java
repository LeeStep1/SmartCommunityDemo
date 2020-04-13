package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.system.Client;
import cn.bit.facade.service.system.ClientFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.system.dao.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("clientFacade")
public class ClientFacadeImpl implements ClientFacade {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public Client addClient(Client client) {
        client.setCreateAt(new Date());
        client.setUpdateAt(client.getCreateAt());
        client.setDataStatus(DataStatusType.VALID.KEY);
        return clientRepository.insert(client);
    }

    @Override
    public Client deleteClient(ObjectId id) {
        Client client = new Client();
        client.setId(id);
        client.setUpdateAt(new Date());
        client.setDataStatus(DataStatusType.INVALID.KEY);
        return clientRepository.updateOne(client);
    }

    @Override
    public Client getClientById(ObjectId id) {
        return clientRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Client> getClients(int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size);
        org.springframework.data.domain.Page<Client> resultPage = clientRepository.findByDataStatus(DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public List<Client> getClientByTypes(List<Integer> types, Integer partner) {
        return clientRepository.findByTypeInAndPartnerAndDataStatus(types, partner, DataStatusType.VALID.KEY);
    }
}
