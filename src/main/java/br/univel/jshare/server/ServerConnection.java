package br.univel.jshare.server;

import br.univel.jshare.Main;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;
import br.univel.jshare.observers.ServerObserver;
import com.sun.security.ntlm.Server;

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
    private List<ServerObserver> observers = new ArrayList<>();

    private Main main;
    private Registry registry;
    private IServer service;
    private Map<Cliente, List<Arquivo>> defaultMap = new HashMap<>();

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }

    public void addObserver(final ServerObserver observer) {
        Objects.requireNonNull(observer, "Observer cannot be null");
        this.observers.add(observer);
    }

    public void notifyObservers(final String text) {
        Objects.requireNonNull(text, "Text message cannot be null");
        observers.forEach(observer -> observer.showLogInformation(text));
    }

    @Override
    public void registrarCliente(final Cliente c) throws RemoteException {
        Objects.requireNonNull(c, "Client cannot be null");

        if (defaultMap.containsKey(c)) {
            notifyObservers("Client already registered");
        } else {
            defaultMap.put(c, null);
            notifyObservers("Client " + c.getNome() + " has registered with ip " + c.getIp());
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

            notifyObservers("Client " + c.getNome() + " published new files");
        } else {
            notifyObservers("Client not found");
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
            notifyObservers("Client " + cli.getNome() + " downloaded file");

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

            notifyObservers("Client " + c.getNome() + " with IP " + c.getIp() + " is Offline");
        } else {
            notifyObservers("Client not found");
        }
    }

    public void startServer() {
        try {
            System.setProperty("java.rmi.server.hostname", this.main.getIP());
        	service = (IServer) UnicastRemoteObject.exportObject(this.main.getServerConnection(), 0);
            registry = LocateRegistry.createRegistry(this.main.getPort());
            registry.rebind(IServer.NOME_SERVICO, service);


            notifyObservers("Server is Online on IP " + this.main.getIP() + " Port " + this.main.getPort());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        try {
            registry.unbind(IServer.NOME_SERVICO);

            notifyObservers("Server is Offline");
        } catch (RemoteException | NotBoundException  e) {
            throw new RuntimeException(e);
        }
    }
}
