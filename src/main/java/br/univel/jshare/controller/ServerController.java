package br.univel.jshare.controller;

import br.univel.jshare.Main;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Created by felipefrizzo on 02/04/17.
 */
public class ServerController implements IServer {
    private Main main;
    private Map<Cliente, List<Arquivo>> clientMap = new HashMap<>();

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");

        this.main = main;
    }

    @Override
    public void registrarCliente(Cliente c) throws RemoteException {
        Objects.requireNonNull(c, "Cliente cannot be null");

        List<Arquivo> list = new ArrayList<>();
        if (!clientMap.containsKey(c)) {
            clientMap.put(c, list);
            System.out.println("Client " + c.getNome() + "registered");
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

    public void startServer() {
        ServerController server = new ServerController();

        IServer service;

        try {
            System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
            service = (IServer) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(8080);
            registry.bind(IServer.NOME_SERVICO, service);

            this.main.setServer(service);
            this.main.setRegistryServer(registry);

            System.out.println("Server is Online on IP: " + InetAddress.getLocalHost().getHostAddress());

        } catch (RemoteException | AlreadyBoundException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        try {
            this.main.getRegistryServer().unbind(IServer.NOME_SERVICO);

            System.out.println("Server is Offline");

            this.main.setRegistryServer(null);
            this.main.setRegistryClient(null);
            this.main.setServer(null);
            this.main.setClient(null);
        } catch (RemoteException | NotBoundException  e) {
            throw new RuntimeException(e);
        }
    }

    public void connectServer(final String ip, final Integer port) {
        Objects.requireNonNull(ip, "IP cannot be null");
        Objects.requireNonNull(port, "Port cannot be null");

        Cliente cliente = new Cliente();
        try {
            cliente.setNome("FFrizzo");
            cliente.setIp(InetAddress.getLocalHost().getHostAddress());
            cliente.setPorta(8080);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(ip, port);
            IServer service = (IServer) registry.lookup(IServer.NOME_SERVICO);

            this.main.setRegistryClient(registry);
            this.main.setClient(service);
            registrarCliente(cliente);

            System.out.println("Connect on server");
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
