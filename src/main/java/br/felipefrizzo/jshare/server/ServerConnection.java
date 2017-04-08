package br.felipefrizzo.jshare.server;

import br.felipefrizzo.jshare.Main;
import br.felipefrizzo.jshare.comum.Arquivo;
import br.felipefrizzo.jshare.comum.Cliente;
import br.felipefrizzo.jshare.comum.IServer;
import br.felipefrizzo.jshare.comum.TipoFiltro;

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
 * Created by felipefrizzo on 06/04/17.
 */
public class ServerConnection implements IServer {
    private Main main;
    private Registry registry;
    private IServer service;
    private Map<Cliente, List<Arquivo>> defaultMap = new HashMap<>();

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }


    @Override
    public void registrarCliente(final Cliente c) throws RemoteException {
        Objects.requireNonNull(c, "Client cannot be null");

        if (defaultMap.containsKey(c)) {
            System.out.println("Client already registered");
        } else {
            defaultMap.put(c, null);
            System.out.println("Client " + c.getNome() + " has registered with ip " + c.getIp());
        }
    }

    @Override
    public void publicarListaArquivos(final Cliente c, final List<Arquivo> lista) throws RemoteException {
        Objects.requireNonNull(c, "Client cannot be null");
        Objects.requireNonNull(lista, "List of Files cannot be null");

        if (defaultMap.containsKey(c)) {
            defaultMap.entrySet().forEach(map -> {
                if (map.getKey().equals(c)) {
                    map.setValue(lista);
                }
            });

            System.out.println("Client " + c.getNome() + " published new files");
        } else {
            System.out.println("Client not found");
        }
    }

    @Override
    public Map<Cliente, List<Arquivo>> procurarArquivo(final String query, final TipoFiltro tipoFiltro, final String filtro) throws RemoteException {
        Objects.requireNonNull(query, "Query cannot be null");
        Objects.requireNonNull(tipoFiltro, "Filter type cannot be null");
        Objects.requireNonNull(filtro, "Filter cannot be null");


        Map<Cliente, List<Arquivo>> resultMap = new HashMap<>();

        defaultMap.forEach((key, value) -> {
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
    public byte[] baixarArquivo(final Cliente cli, final Arquivo arq) throws RemoteException {
        Objects.requireNonNull(cli, "Client cannot be null");
        Objects.requireNonNull(arq, "Files cannot be null");

        byte[] data;
        Path path = Paths.get(arq.getPath());

        try {
            data = Files.readAllBytes(path);
            System.out.println("Client " + cli.getNome() + " downloaded file");

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void desconectar(final Cliente c) throws RemoteException {
        Objects.requireNonNull(c, "Client cannot be null");

        if (defaultMap.containsKey(c)) {
            defaultMap.remove(c);

            System.out.println("Client " + c.getNome() + " with IP " + c.getIp() + " is Offline");
        } else {
            System.out.println("Client not found");
        }
    }

    public void startServer() {
        ServerConnection server = new ServerConnection();

        try {
            System.setProperty("java.rmi.server.hostname", this.main.getIP());
            service = (IServer) UnicastRemoteObject.exportObject(server, 0);
            registry = LocateRegistry.createRegistry(this.main.getPort());
            registry.bind(IServer.NOME_SERVICO, service);


            System.out.println("Server is Online on IP " + this.main.getIP() + " Port " + this.main.getPort());

        } catch (RemoteException | AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        try {
            registry.unbind(IServer.NOME_SERVICO);

            System.out.println("Server is Offline");
        } catch (RemoteException | NotBoundException  e) {
            throw new RuntimeException(e);
        }
    }
}
