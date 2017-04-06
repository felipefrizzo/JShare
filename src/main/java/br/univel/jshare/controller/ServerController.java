package br.univel.jshare.controller;

import br.univel.jshare.Main;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;
import br.univel.jshare.validator.MD5Validator;

import java.io.File;
import java.io.IOException;
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
            System.out.println("Client " + c.getNome() + " registered");
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
            System.out.println(c.getNome() + " published new updates");
        } else {
            System.out.println("Client not found");
        }
    }

    @Override
    public Map<Cliente, List<Arquivo>> procurarArquivo(String query, TipoFiltro tipoFiltro, String filtro) throws RemoteException {
        Map<Cliente, List<Arquivo>> resultMap = new HashMap<>();

        clientMap.forEach((key, value) -> {
            List<Arquivo> list = new ArrayList<>();

            value.forEach(v -> {
                switch (tipoFiltro) {
                    case NOME:
                        if (v.getNome().toLowerCase().contains(query.toLowerCase())) {
                            list.add(v);
                        }
                        break;
                    case EXTENSAO:
                        if (v.getExtensao().toLowerCase().contains(filtro.toLowerCase())) {
                            if (v.getNome().toLowerCase().contains(query.toLowerCase())) {
                                list.add(v);
                            }
                        }
                        break;
                    case TAMANHO_MAX:
                        if (v.getTamanho() >= Integer.valueOf(filtro)) {
                            if (v.getNome().toLowerCase().contains(query.toLowerCase())) {
                                list.add(v);
                            }
                        }
                        break;
                    case TAMANHO_MIN:
                        if (v.getTamanho() <= Integer.valueOf(filtro)) {
                            if (v.getNome().toLowerCase().contains(query.toLowerCase())) {
                                list.add(v);
                            }
                        }
                        break;
                }
            });

            resultMap.put(key, list);
        });

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
            System.setProperty("java.rmi.server.hostname", this.main.getPORT());
            service = (IServer) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(8080);
            registry.bind(IServer.NOME_SERVICO, service);

            this.main.setServer(service);
            this.main.setRegistryServer(registry);

            System.out.println("Server is Online on IP: " + this.main.getPORT());

        } catch (RemoteException | AlreadyBoundException e) {
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

        Registry registry;

        try {
            registry = LocateRegistry.getRegistry(ip, port);
            IServer service = (IServer) registry.lookup(IServer.NOME_SERVICO);

            this.main.setRegistryClient(registry);
            this.main.setClient(service);

            this.main.getClient().registrarCliente(this.main.getClienteGlobal());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        uploadFile();

                        try {
                            Thread.sleep(100000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();

            System.out.println("Connect on server");
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Arquivo> listLocalFiles() {
        File fileDir = new File("." + File.separatorChar + "shared" + File.separatorChar);

        List<Arquivo> listFiles = new ArrayList<>();
        for (File f : fileDir.listFiles()) {
            if (f.isFile()) {
                Arquivo a = new Arquivo();

                a.setNome(f.getName());
                a.setPath(f.getPath());
                a.setTamanho(f.length());
                a.setMd5(MD5Validator.getMD5Checksum(f.getPath()));
                a.setDataHoraModificacao(new Date(f.lastModified()));
                String extensao = f.getName().substring(f.getName().lastIndexOf("."), f.getName().length());
                a.setExtensao(extensao);

                listFiles.add(a);
            }
        }

        return listFiles;
    }

    public void uploadFile() {
        List<Arquivo> list = listLocalFiles();

        try {
            this.main.getClient().publicarListaArquivos(this.main.getClienteGlobal(), list);

            System.out.println("Update list file");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }
}
