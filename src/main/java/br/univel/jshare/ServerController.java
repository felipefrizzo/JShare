package br.univel.jshare;

import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by felipefrizzo on 02/04/17.
 */
public class ServerController implements IServer {
    private Map<Cliente, List<Arquivo>> clientMap = new HashMap<>();

    @Override
    public void registrarCliente(Cliente c) throws RemoteException {
        Objects.requireNonNull(c, "Cliente cannot be null");

        List<Arquivo> list = new ArrayList<>();
        if (!clientMap.containsKey(c)) {
            clientMap.put(c, list);
            System.out.println("Client registered");
        } else {
            System.out.println("Client already registered");
        }
    }

    @Override
    public void publicarListaArquivos(Cliente c, List<Arquivo> lista) throws RemoteException {
        Objects.requireNonNull(c, "Cliente cannot be null");
        Objects.requireNonNull(lista, "Lista cannot be null");

        if (clientMap.containsKey(c)) {
            clientMap.entrySet().forEach(map -> {
                if (map.getKey().equals(c)) {
                    map.setValue(lista);
                }
            });
        } else {
            System.out.println("Client not found");
        }
    }

    @Override
    public Map<Cliente, List<Arquivo>> procurarArquivo(String query, TipoFiltro tipoFiltro, String filtro) throws RemoteException {
        Map<Cliente, List<Arquivo>> resultMap = new HashMap<>();
        List<Arquivo> list = new ArrayList<>();

        for (Map.Entry<Cliente, List<Arquivo>> clientListEntry : clientMap.entrySet()) {
            Cliente client = new Cliente();
            client.setNome(clientListEntry.getKey().getNome());
            client.setId(clientListEntry.getKey().getId());
            client.setPorta(clientListEntry.getKey().getPorta());

            list.clear();
            for (Arquivo file : clientListEntry.getValue()) {
                switch (tipoFiltro) {
                    case NOME:
                        if (file.getNome().contains(query)) {
                            list.add(file);
                        }
                        break;
                    case EXTENSAO:
                        if (file.getExtensao().contains(filtro)) {
                            if (file.getNome().contains(query)) {
                                list.add(file);
                            }
                        }
                        break;
                    case TAMANHO_MAX:
                        if (file.getTamanho() <= Integer.valueOf(filtro)) {
                            if (file.getNome().contains(query)) {
                                list.add(file);
                            }
                        }
                        break;
                    case TAMANHO_MIN:
                        if (file.getTamanho() >= Integer.valueOf(filtro)) {
                            if (file.getNome().contains(query)) {
                                list.add(file);
                            }
                        }
                        break;
                    default:
                        list.add(file);
                        break;
                }
            }
            resultMap.put(client, list);
        }
        return resultMap;
    }

    @Override
    public byte[] baixarArquivo(Cliente cli, Arquivo arq) throws RemoteException {
        Objects.requireNonNull(cli, "Cliente cannot be null");
        Objects.requireNonNull(arq, "File cannot be null");

        byte[] data;
        Path path = Paths.get(arq.getPath());

        try {
            data = Files.readAllBytes(path);
            System.out.println("User:" + cli.getNome() + " with IP:" + cli.getIp() + " downloaded your file");

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void desconectar(Cliente c) throws RemoteException {
        Objects.requireNonNull(c, "Cliente cannot be null");

        if (clientMap.containsKey(c)) {
            clientMap.remove(c);
            System.out.println("User:" + c.getNome() + " with IP:" + c.getIp() + " is offline");
        } else {
            System.out.println("Client not found");
        }
    }
}
