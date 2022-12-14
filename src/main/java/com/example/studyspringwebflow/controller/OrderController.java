package com.example.studyspringwebflow.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import com.example.studyspringwebflow.entity.*;
import com.example.studyspringwebflow.entity.support.OrderBuilder;
import com.example.studyspringwebflow.listener.AuthenticationSessionListener;
import com.example.studyspringwebflow.service.BookstoreService;
import com.example.studyspringwebflow.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OrderController {

	@Autowired
	private BookstoreService bookstoreService;

	@Autowired
	private CategoryService categoryService;

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");

	@RequestMapping("ordersOverview")
	public ModelAndView retrieveOrders(HttpSession httpSession) {
		List<Order> orders = bookstoreService.findOrdersForAccount((Account) httpSession
				.getAttribute(AuthenticationSessionListener.AUTHENTICATED_ACCOUNT_KEY));

		ModelAndView mov = new ModelAndView();
		mov.setViewName("ordersOverview");
		mov.getModel().put("orders", orders);

		return mov;
	}

	public OrderForm initializeForm() {
		OrderForm orderForm = new OrderForm();
		orderForm.setQuantity(1);
		orderForm.setOrderDate(simpleDateFormat.format(new Date()));
		return orderForm;
	}

	public Map<Long, String> initializeSelectableCategories() {
		Map<Long, String> selectableCategories = new HashMap<Long, String>();
		for (Category category : categoryService.findAll()) {
			selectableCategories.put(category.getId(), category.getName());
		}

		return selectableCategories;
	}

	public Map<Long, String> initializeSelectableBooks(OrderForm orderForm) {
		orderForm.getSelectedBooks().clear();
		orderForm.resetSelectedBooks();

		Map<Long, String> selectableBooks = new HashMap<Long, String>();
		for (Book book : bookstoreService.findBooksByCategory(categoryService.findById(orderForm.getCategoryId()))) {
			selectableBooks.put(book.getId(), book.getTitle());
		}

		return selectableBooks;
	}

	public void addBook(OrderForm orderForm) {
		Book book = bookstoreService.findBook(orderForm.getBookId());
		if (orderForm.getSelectedBooks().containsKey(book)) {
			orderForm.getSelectedBooks().put(book, orderForm.getSelectedBooks().get(book) + orderForm.getQuantity());
		} else {
			orderForm.getSelectedBooks().put(book, orderForm.getQuantity());
		}
	}

	public void placeOrder(final OrderForm orderForm, final Account account) throws ParseException {
		OrderBuilder orderBuilder = new OrderBuilder() {
			{
				deliveryDate(simpleDateFormat.parse(orderForm.getDeliveryDate()));
				account(account);
				orderDate(simpleDateFormat.parse(orderForm.getOrderDate()));
			}
		};

		for (Entry<Book, Integer> selectedBook : orderForm.getSelectedBooks().entrySet()) {
			orderBuilder.addBook(selectedBook.getKey(), selectedBook.getValue());
		}

		bookstoreService.store(orderBuilder.build(true));
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

}
