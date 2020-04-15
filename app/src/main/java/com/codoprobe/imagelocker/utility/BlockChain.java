package com.codoprobe.imagelocker.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * The fundamental idea of a BlockChain is logically connected blocks
 * which represent transactions or entries. In this case we are using
 * block chain like pattern to turn a Bitmap into small chunks and then
 * store them into different location in the file system and when need
 * retrieve them and get back the bitmap.
 *
 * @author Ayan Chakraborty
 */

public class BlockChain {

    private static final String TAG = "BLOCKCHAIN";

    /**
     * Block file extension.
     */
    private static final String FILE_EXTENSION = "cilblk";

    /**
     * Byte limit for every block's data attribute.
     */
    private static final int BLOCK_DATA_LENGTH = 1000000;

    /**
     * Size of thumbnail.
     */
    private static final int THUMBNAIL_SIZE = 500;

    /**
     * Maximum file size limit.
     * Current maximum file size (in bitmap from) is 50MB.
     */
    private static final int MAX_FILE_SIZE_LIMIT = 50000000;

    /**
     * Save location of the files.
     */
    private static final String FILE_SAVE_PATH = "/storage/emulated/0/ImageLocker/";

    /**
     * Chain is a model that contains an ArrayList of Block objects
     * (which are logically connected) and also gives methods to get
     * bitmap data from the chain, delete chain and save chain as image.
     */
    public static class Chain {

        private ArrayList<String> fileUriArray;
        private ArrayList<Block> blocks;
        private Bitmap bitmap;

        /**
         * Constructor
         * Send only valid (logically connected) files uris and blocks.
         *
         * @param fileUris File uri ArrayList (constructor assumes the
         *                 files are logically connected).
         * @param blocks   Blocks ArrayList (constructor assumes the
         *                 blocks are logically connected).
         */
        Chain(ArrayList<String> fileUris, ArrayList<Block> blocks) {
            this.fileUriArray = fileUris;
            this.blocks = blocks;

            bitmap = getBitmapFromChain();

        }

        /**
         * Creates a bitmap from the data field of the blocks.
         *
         * @return The bitmap which the BlockChain is containing.
         */
        private Bitmap getBitmapFromChain() {
            Block foundation_block = null;
            for(Block b: blocks) if (b.previous_id.isEmpty())  foundation_block = b;

            if (foundation_block == null) {
                return null;
            }
            else {
                String expected_previous_id = foundation_block.id;

                ArrayList<Block> chain = new ArrayList<Block>();
                StringBuilder encodedString = new StringBuilder();

                chain.add(foundation_block); // adding foundation blocks

                for(int i=0; i<blocks.size(); i++) {
                    if(Objects.equals(blocks.get(i).previous_id, expected_previous_id)) {
                        chain.add(blocks.get(i));
                        expected_previous_id = blocks.get(i).id;
                        i = 0;
                    }
                }

                for(Block b: blocks) encodedString.append(b.data);

                byte[] decodedString = android.util.Base64.decode(encodedString.toString(), android.util.Base64.DEFAULT);

                return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

        }

        /**
         * Get the image(bmp) of the BlockChain.
         *
         * @return Full size bitmap.
         */
        public Bitmap getBitmap() {
            return bitmap;
        }

        /**
         * Returns a small version of the bitmap.
         *
         * @return Small size bitmap.
         */
        public Bitmap getThumbnail() {
            int width = this.getBitmap().getWidth();
            int height = this.getBitmap().getHeight();

            float bitmapRatio = (float)width / (float) height;
            if (bitmapRatio > 1) {
                width = THUMBNAIL_SIZE;
                height = (int) (width / bitmapRatio);
            } else {
                height = THUMBNAIL_SIZE;
                width = (int) (height * bitmapRatio);
            }
            return Bitmap.createScaledBitmap(this.getBitmap(), width, height, true);
        }

        /**
         * Deletes all the files that contain the Block objects of this
         * BlockChain.
         */
        public void delete() {
            for(String uri : fileUriArray) {
                File file = new File(uri);
                file.delete();
            }
        }

        /**
         * Create a .png file from the bitmap and saves in "save_location".
         */
        public void saveFile() {

            File save_location = new File(FILE_SAVE_PATH);

            if (!save_location.exists()) save_location.mkdir();

            try (FileOutputStream out = new FileOutputStream(save_location+"/"+UUID.randomUUID().toString()+".png")) {
                this.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * This method is called when the encryption key is changed. This
         * method will encrypt the Blocks of this chain and save the files.
         *
         * @param key The new key (new password).
         * @throws IOException File saving errors.
         */
        public void changeEncryptionKeyAndSaveBlocks(String key) throws IOException {
            for(int i=0; i<blocks.size(); i++) {

                Block b = blocks.get(i);

                // b.encrypt(key);
                File file = new File(fileUriArray.get(i));

                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(b.encrypt(key));

                myOutWriter.close();

                fOut.flush();
                fOut.close();
            }
        }

        /**
         * Helps loading the chain into to the memory. This class contains
         * only one method i.e. load().
         */
        public static class Loader {

            /**
             * Loads all the chains available in the file system.
             * This is the one and only method in this class.
             *
             * @param key Encryption key.
             * @return Add the chains available.
             * @throws IOException Exception on file load.
             */
            public static ArrayList<Chain> load(String key) throws IOException {

                ArrayList<String> fileUriList = new ArrayList<>();
                getAllFiles("/storage/emulated/0/", fileUriList);

                ArrayList<Block> blocks = new ArrayList<Block>();
                ArrayList<Chain> chains = new ArrayList<Chain>();

                // loading all files as blocks
                for(String uri: fileUriList) {
                    File current_file = new File(uri);

                    StringBuilder text = new StringBuilder();
                    BufferedReader br = new BufferedReader(new FileReader(current_file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                    br.close();

                    Block b = Block.decrypt(text.toString(), key);
                    if (b != null) blocks.add(b);

                }

                // find the foundation block and use findBlockChain method to find rest of the chain and add to chains list.xml
                for (int i=0; i<blocks.size(); i++) {
                    if (blocks.get(i).previous_id.isEmpty()) chains.add(findBlockChain(i, blocks, fileUriList));
                }

                return chains;
            }

            /**
             * Creates a BlockChain objects using the blocks retrieved from
             * the file system. The Chain will be created based on the given
             * "Foundation Block". Other blocks and file uris will be ignored.
             *
             * @param foundation_block_index The first block's index. This can
             *                               be determined by checking the block
             *                               object's "previous_id". If the
             *                               "previous_id" is null then the block
             *                               is a "Foundation Block".
             *
             * @param blocks                 All the blocks. Blocks unrelated to
             *                               "Foundation Block" will be ignored.
             * @param fileUriList            All the file uris. Uris unrelated to
             *                               "Foundation Block" will be ignored.
             * @return Chain based on the foundation block.
             */
            private static Chain findBlockChain(int foundation_block_index,
                                                ArrayList<Block> blocks,
                                                ArrayList<String> fileUriList) {
                String expected_previous_id = blocks.get(foundation_block_index).id;

                ArrayList<Block> chain_blocks = new ArrayList<Block>();
                ArrayList<String> chain_block_uris = new ArrayList<String>();


                chain_blocks.add(blocks.get(foundation_block_index)); // adding foundation blocks
                chain_block_uris.add(fileUriList.get(foundation_block_index));

                for(int i=0; i<blocks.size(); i++) {
                    if(Objects.equals(blocks.get(i).previous_id, expected_previous_id)) {
                        chain_blocks.add(blocks.get(i));
                        chain_block_uris.add(fileUriList.get(i));

                        expected_previous_id = blocks.get(i).id;
                        i = 0;
                    }
                }

                return new Chain(chain_block_uris, chain_blocks);
            }

            /**
             * Finds all the .cilblk from the android system except 'Android/data'
             * folders.
             *
             * @param directoryName Initial directory.
             * @param files         Overrides the parameter (Send an empty ArrayList).
             */
            private static void getAllFiles(String directoryName, ArrayList<String> files) {
                File directory = new File(directoryName);

                File[] fList = directory.listFiles();
                if(fList != null){
                    for (File file : fList) {
                        if(file.isFile()) {
                            try {
                                if(file.getName()
                                        .split("\\.")[1]
                                        .equals(FILE_EXTENSION))
                                    files.add(file.toString());
                            }
                            catch (Exception e) {}

                        }
                        else if(file.isDirectory()) getAllFiles(file.getAbsolutePath(), files);
                    }
                }
            }

        }

        /**
         * Helps build a chain and save to the file system. This class will
         * let you create a BlockChain from a Bitmap.
         */
        public static class Builder {

            private byte[] bytesArray; // holds bitmap in bytes
            private String encodedString; // holds Base64 string created from bytes

            private Bitmap bmp; // file origin uri
            private String key; // encryption key

            private ArrayList<Block> blocks; // blocks of the blockchain
            private ArrayList<String> filePaths; // contains the files destination location (mapped to 'blocks')

            /**
             * This constructor uses the bitmap to create a Builder object.
             *
             * @param _bmp  Bitmap of the file.
             * @param key   Encryption key.
             */
            public Builder(Bitmap _bmp, String key) {
                this.bmp = _bmp;
                this.key = key;
            }

            /**
             * This constructor uses the file uri to create a Builder
             * object. This method also assigns the private bitmap
             * field.
             *
             * @param uri URI of the target file.
             * @param key Encryption key.
             */
            public Builder(String uri, String key) {
                this.bmp = BitmapFactory.decodeFile(uri);
                this.key = key;
            }

            /**
             * Creates a Chain using the private bitmap field. This
             * method throws an exception IO exception if the length
             * of the file is bigger than MAX_FILE_SIZE_LIMIT.
             *
             * @return Chain built using the bitmap.
             * @throws IOException if the block didn't save properly
             *                     or the file length was too bit.
             */
            public Chain build() throws IOException {
                if (this.bmp.getByteCount() < MAX_FILE_SIZE_LIMIT) {
                    // creating Base64 string
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    bytesArray = stream.toByteArray();

                    encodedString = new String(
                            android.util.Base64.encode(bytesArray, android.util.Base64.DEFAULT));

                    // getting blocks
                    blocks = getBlocksFromEncodedStringChunks(
                            getEncodedStringChunks(encodedString));

                    // saving blocks and storing path references
                    filePaths = save();

                    return new Chain(filePaths, blocks);
                }
                else throw new IOException("File is too big. process stopped.");
            }

            /**
             * Creates list.xml of chunks from the Base64 String based
             * on the BLOCK_DATA_LENGTH.
             *
             * @param encodedString Encoded string of bitmap.
             * @return ArrayList of chunks of encoded String.
             */
            private ArrayList<String> getEncodedStringChunks(String encodedString) {
                ArrayList<String> chunks = new ArrayList<String>();

                for(int i=0; i<encodedString.length(); i+= BLOCK_DATA_LENGTH) {
                    int end_index = 0;

                    if(i+ BLOCK_DATA_LENGTH < encodedString.length()) {
                        end_index = i+ BLOCK_DATA_LENGTH;
                    } else { end_index = encodedString.length(); }

                    chunks.add(encodedString.substring(i, end_index));
                }

                return chunks;
            }

            /**
             * Creates Blocks using the chunks of the the encoded
             * string.
             *
             * @param chunks ArrayList of chunks of encoded string.
             * @return ArrayList of Blocks.
             */
            private ArrayList<BlockChain.Block> getBlocksFromEncodedStringChunks(
                    ArrayList<String> chunks) {
                ArrayList<BlockChain.Block> blocks = new ArrayList<BlockChain.Block>();

                String previous_id = "";

                for (String data : chunks) {
                    String current_id = UUID.randomUUID().toString();
                    blocks.add(new BlockChain.Block(
                            current_id,
                            previous_id,
                            data,
                            Calendar.getInstance().getTimeInMillis()));

                    previous_id = current_id;
                }

                return blocks;
            }

            /**
             * Saves the blocks as files.
             *
             * @return Files path list.xml.
             * @throws IOException Exception on saving operation.
             */
            private ArrayList<String> save() throws IOException {

                ArrayList<String> _filePaths = new ArrayList<String>();

                // getting directories
                ArrayList<File> files = new ArrayList<File>();
                getAllDirectories("/storage/emulated/0/", files);

                Random randomGenerator = new Random();

                for(int i=0; i<blocks.size(); i++) {
                    Block b = blocks.get(i);

                    String filename = UUID.randomUUID()+"."+FILE_EXTENSION ; // extension

                    File parent_location = files.get(randomGenerator.nextInt(files.size()));
                    Log.e(TAG, "save: "+parent_location.getAbsolutePath());

                    File file = new File(parent_location, filename);

                    file.createNewFile();

                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(b.encrypt(key));

                    myOutWriter.close();

                    fOut.flush();
                    fOut.close();

                    _filePaths.add(file.getAbsolutePath());
                }

                return _filePaths;
            }

            /**
             * Finds all the directories from the android system except
             * 'Android/data' folders.
             *
             * @param directoryName Initial directory.
             * @param files         Overrides the parameter (Send an
             *                      empty ArrayList).
             */
            private void getAllDirectories(String directoryName, ArrayList<File> files) {
                File directory = new File(directoryName);

                // Get all files from a directory.
                File[] fList = directory.listFiles();
                if(fList != null){
                    for (File file : fList) {

                        // Ignoring cache data folder
                        if (file.isDirectory() &&
                            !file.getAbsolutePath().matches("(.*)Android/data(.*)")) {
                            files.add(file);
                            getAllDirectories(file.getAbsolutePath(), files);
                        }

                    }
                }
            }

        }
    }

    /**
     * Block is a model that contains chunks of the Base64 and has
     * an id and an previous_id to form a chain to get back the data.
     * Blocks are stored by spreading them in the file system. This
     * means that the blocks are not in a single location. Rather they
     * are in various folders of the file system. The blocks are saved
     * in a .cilblk files, which are encrypted JSON file.
     *
     * This class only handles Creating a block, Creating data for the
     * .cilblk file (saving this is file is done by the Chain Builder class
     * {@see Chain.Builder}) and Converting data from a .ccilblk file to
     * a block object (loading the file and getting data from the file
     * is done by the Chain Loader class {@see Chain.Loader}).
     *
     */
    public static class Block {

        private String id, data, previous_id;
        private long timestamp;

        /**
         *
         * @param id          UUID for the block.
         * @param previous_id Previous block's UUID.
         * @param data        Chunk of the Base64 string.
         * @param timestamp   Timestamp.
         */
        public Block(String id, String previous_id, String data, long timestamp) {
            this.id = id;
            this.previous_id = previous_id;
            this.data = data;
            this.timestamp = timestamp;
        }

        /**
         * This method first creates a JSON string using the fields and
         * then encrypts the string using the key and returns the cipher.
         *
         * @param key Encryption key.
         * @return Cipher string or null if there was an encryption error.
         */
        public String encrypt(String key) {
            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", this.id);
                jsonObject.put("previous_id", this.previous_id);
                jsonObject.put("data", this.data);
                jsonObject.put("timestamp", this.timestamp);

                String value = jsonObject.toString();

                SecretKeySpec skeySpec = new SecretKeySpec(
                        key.getBytes(StandardCharsets.UTF_8), "AES");

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

                byte[] encrypted = android.util.Base64
                        .encode(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)),
                        android.util.Base64.DEFAULT);

                return new String(encrypted);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * This method decrypts the encrypted string (data from .cilblk
         * file) using the encryption key and creates Block objects.
         *
         * @param encrypted Data from .cilblk file.
         * @param key       Encryption Key.
         * @return Block object or null if there was an error, data
         * inconsistency or encryption error
         */
        public static BlockChain.Block decrypt(String encrypted, String key) {
            try {

                SecretKeySpec skeySpec = new SecretKeySpec(
                        key.getBytes(StandardCharsets.UTF_8), "AES");

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);

                String original = (new String(cipher
                        .doFinal(android.util.Base64.decode(encrypted, Base64.DEFAULT))));

                JSONObject jsonObject = new JSONObject(original);

                return new BlockChain.Block(jsonObject.getString("id"),
                        jsonObject.getString("previous_id"),
                        jsonObject.getString("data"),
                        jsonObject.getLong("timestamp"));

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}
