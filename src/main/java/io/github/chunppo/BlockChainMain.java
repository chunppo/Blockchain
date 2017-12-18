package io.github.chunppo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.deploy.util.StringUtils;
import lombok.Data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Project : blockchain
 * @Date : 2017-12-15
 * @Author : chunppo
 * @Description :
 */

class BlockUtil {
    private final static String ENCODE_MODE = "SHA-256";

    public static String encryptSHA256(String encodeString) {
        String result = null;
        try {
            MessageDigest sh = MessageDigest.getInstance(ENCODE_MODE);
            sh.update(encodeString.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();

            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No Search Algorithm Exception");
        }

        return result;
    }
}

@Data
class Block {
    private int index;
    private String timestamp;
    private String data;
    private String previousHash = "";
    private String hash = "";
    private int nonce = 0;

    Block(int index, String timestamp, String data, String previousHash) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.hash = this.calculateHash();
    }

    public String calculateHash() {
        String encrypyString = this.index + this.timestamp.toString() + this.data + this.nonce;
        String result = BlockUtil.encryptSHA256(encrypyString);

        return result;
    }

    public void mineBlock(int difficulty) {
        List<String> checkList = new ArrayList<>();
        for (int i = 0; i < difficulty; i++) {
            checkList.add("0");
        }

        while(this.hash.substring(0, difficulty).equals(StringUtils.join(checkList, "")) != true) {
            this.nonce++;
            this.hash = this.calculateHash();
        }
    }
}

@Data
class BlockChain {
    private List<Block> chain = new ArrayList<>();
    private int difficulty = 5;

    BlockChain() {
        chain.add(this.createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block(0,"2017/01/01", "Genesis block", "0");
    }

    private Block getLastBlock() {
        return this.chain.get(this.chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        newBlock.setPreviousHash(this.getLastBlock().getHash());
//        newBlock.setHash(newBlock.calculateHash());
        newBlock.mineBlock(difficulty);
        this.chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < this.chain.size(); i++) {
            Block currentBlock = this.chain.get(i);
            Block previousBlock = this.chain.get(i - 1);

            if (currentBlock.getHash().equals(currentBlock.calculateHash()) != true) {
                return false;
            }

            if (currentBlock.getPreviousHash().equals(previousBlock.getHash()) != true) {
                return false;
            }
        }

        return true;
    }
}

public class BlockChainMain {

    public static void main(String[] args) throws JsonProcessingException {
        BlockChain blockChain = new BlockChain();
        blockChain.addBlock(new Block(1, "2017/12/15", "{ amount: 10 }", ""));
        blockChain.addBlock(new Block(2, "2017/12/11", "{ amount: 1 }", ""));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(blockChain));
        System.out.println("Is blockchain valid? " + blockChain.isChainValid());
    }
}