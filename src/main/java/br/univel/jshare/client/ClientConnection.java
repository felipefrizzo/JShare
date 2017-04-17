package br.univel.jshare.client;

import br.univel.jshare.Main;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.validator.MD5Validator;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by felipefrizzo on 06/04/17.
 */
public class ClientConnection {
    private Main main;
    private Registry registry;
    private IServer service;

    public Registry getRegistry() {
        return registry;
    }

    public IServer getService() {
        return service;
    }

    public void setMain(final Main main) {
        Objects.requireNonNull(main, "Main class cannot be null");
        this.main = main;
    }

    public void connect(final String ip, final Integer port) {
        Objects.requireNonNull(ip, "Ip cannot be null");
        Objects.requireNonNull(port, "Port cannot be null");

        try {
            registry = LocateRegistry.getRegistry(ip, port);
            service = (IServer) registry.lookup(IServer.NOME_SERVICO);

            service.registrarCliente(main.getDefaultClient());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        uploadFiles();

                        try {
                            Thread.sleep(100000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Arquivo> getLocalFiles() {
        File directory = new File("." + File.separatorChar + "shared" + File.separatorChar);
        List<Arquivo> files = new ArrayList<>();

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                Arquivo a = new Arquivo();

                a.setNome(file.getName().substring(0, file.getName().lastIndexOf(".")));
                a.setPath(file.getPath());
                a.setTamanho(file.length());
                a.setMd5(MD5Validator.getMD5Checksum(file.getPath()));
                a.setDataHoraModificacao(new Date(file.lastModified()));
                String extensao = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
                a.setExtensao(extensao);

                files.add(a);
            }
        }

        return files;
    }

    private void uploadFiles() {
        try {
            service.publicarListaArquivos(main.getDefaultClient(), getLocalFiles());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
