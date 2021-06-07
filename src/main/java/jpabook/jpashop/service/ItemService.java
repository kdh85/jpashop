package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = false)
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findeOne(Long itemId){
        return itemRepository.findOne(itemId);
    }

    @Transactional
    public Item updateItemBook(Book updateBook) {
        Book findBook = (Book) itemRepository.findOne(updateBook.getId());//조회 쿼리를 사용해서 영속성을 획득. set에 의한 변경감지로 자동으로 내부에서 update 쿼리 생성.
        findBook.setAuthor(updateBook.getAuthor());
        findBook.setIsbn(updateBook.getIsbn());
        findBook.setName(updateBook.getName());
        findBook.setPrice(updateBook.getPrice());
        findBook.setStockQuantity(updateBook.getStockQuantity());
        return  findBook;
    }

    @Transactional
    public Item updateItem(Long id, String name, int price, int stockQuantity) {

        Item findItem = itemRepository.findOne(id);
        //findItem.setStockQuantity(stockQuantity);
        //findItem.setPrice(price);
        findItem.changeParam(name, price, stockQuantity);//파라메터가 많다면 DTO를 만들어서 넘기는것을 추천.entity는 안된다.

        return  findItem;
    }
}
