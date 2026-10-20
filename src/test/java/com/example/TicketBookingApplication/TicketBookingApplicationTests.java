package com.example.TicketBookingApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.TicketBookingApplication.DTOs.CommentDTO;
import com.example.TicketBookingApplication.DTOs.MovieRequestDTO;
import com.example.TicketBookingApplication.DTOs.RegisterRequestDTO;
import com.example.TicketBookingApplication.DTOs.RowConfig;
import com.example.TicketBookingApplication.DTOs.SeatLayoutRequestDTO;
import com.example.TicketBookingApplication.DTOs.SeatResponseDTO;
import com.example.TicketBookingApplication.DTOs.ShowRequestDTO;
import com.example.TicketBookingApplication.DTOs.TheatreOwnerDTO;
import com.example.TicketBookingApplication.Entities.Movie;
import com.example.TicketBookingApplication.Entities.Seats;
import com.example.TicketBookingApplication.Entities.ShowSeat;
import com.example.TicketBookingApplication.Entities.Shows;
import com.example.TicketBookingApplication.Entities.TheatreOwnerRequest;
import com.example.TicketBookingApplication.Entities.User;
import com.example.TicketBookingApplication.Entities.Venue;
import com.example.TicketBookingApplication.Enums.MovieCategory;
import com.example.TicketBookingApplication.Enums.RequestStatus;
import com.example.TicketBookingApplication.Enums.Role;
import com.example.TicketBookingApplication.Enums.SeatType;
import com.example.TicketBookingApplication.Repository.BookingRepository;
import com.example.TicketBookingApplication.Repository.MovieRepository;
import com.example.TicketBookingApplication.Repository.SeatRepository;
import com.example.TicketBookingApplication.Repository.ShowSeatsRepository;
import com.example.TicketBookingApplication.Repository.ShowsRepository;
import com.example.TicketBookingApplication.Repository.TheatreOwnerRequestRepo;
import com.example.TicketBookingApplication.Repository.UserRepository;
import com.example.TicketBookingApplication.Repository.VenueRepository;
import com.example.TicketBookingApplication.UtilityClass.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc 
class TicketBookingApplicationTests {

	@Autowired 
	MockMvc mockMvc;

	@Autowired 
	PasswordEncoder passwordEncoder;

	@Autowired 
	UserRepository userRepository;

	@Autowired 
	ObjectMapper objectMapper;

	@Autowired 
	MovieRepository movieRepository;

	@Autowired 
	VenueRepository venueRepository;

	@Autowired 
	JwtUtil jwtUtil;

	@Autowired 
	TheatreOwnerRequestRepo theatreOwnerRequestRepo;

	@Autowired 
	BookingRepository bookingRepository;

	@Autowired 
	SeatRepository seatRepository;

	@Autowired 
	ShowsRepository showsRepository;

	@Autowired 
	ShowSeatsRepository showSeatsRepository;


	@AfterEach 
	public void clearData(){
		showSeatsRepository.deleteAll();
		showsRepository.deleteAll();
		userRepository.deleteAll();
		movieRepository.deleteAll();
		seatRepository.deleteAll();
		venueRepository.deleteAll();
	}

	@BeforeEach 
	public void clearDataBefore(){
		showSeatsRepository.deleteAll();
		showsRepository.deleteAll();
		userRepository.deleteAll();
		movieRepository.deleteAll();
		theatreOwnerRequestRepo.deleteAll();
		seatRepository.deleteAll();
		venueRepository.deleteAll();
	}

	@Test 
	public void testLogin() throws Exception{
		User user=new User();
		user.setName("Loki");
		user.setPassword(passwordEncoder.encode("Logesh@427"));
		user.setRole(Role.ADMIN);
		userRepository.save(user);

		String requestJson="{\"name\":\"Loki\",\"password\":\"Logesh@427\"}";


		MvcResult response=mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk()).andReturn();

		String token=response.getResponse().getContentAsString();

		assertThat(token).isNotNull();
		assertThat(token).isNotBlank();

		assertThat(token.split("\\.")).hasSize(3);
	}


	@Test 
	public void testRegister() throws Exception{

		RegisterRequestDTO registerRequestDTO=new RegisterRequestDTO();
		registerRequestDTO.setEmail("Logesh@tcs.com");
		registerRequestDTO.setName("Loki");
		registerRequestDTO.setPassword("Logesh@427");

		String requestJson=objectMapper.writeValueAsString(registerRequestDTO);

		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isCreated()).andReturn();

		User user=userRepository.findByEmail("Logesh@tcs.com").orElseThrow();

		assertThat(user.getName()).isEqualTo("Loki");
		assertThat(user.getRole()).isEqualTo(Role.MEMBER);
		assertThat(user.getPassword()).isNotEqualTo("Logesh@427");
		assertThat(passwordEncoder.matches("Logesh@427", user.getPassword())).isTrue();


	}

	@Test 
	public void testAddMovie() throws Exception{

		User user=new User();
		user.setName("Loki");
		user.setPassword(passwordEncoder.encode("Logesh@427"));
		user.setRole(Role.ADMIN);
		userRepository.save(user);

		String token=jwtUtil.generateToken(user.getName());


		MovieRequestDTO movieRequestDTO=new MovieRequestDTO();
		movieRequestDTO.setCategory(MovieCategory.ACTION);
		movieRequestDTO.setCensorRating("U");
		movieRequestDTO.setDescription("Action flim");
		movieRequestDTO.setDuration(135);
		movieRequestDTO.setLanguage("English");
		movieRequestDTO.setPosterUrl("dfcvbjnkm,");
		movieRequestDTO.setReleaseDate(LocalDate.of(2029, 12, 12));
		movieRequestDTO.setTitle("Om");

		String requestJson=objectMapper.writeValueAsString(movieRequestDTO);

		mockMvc.perform(post("/admin/add").header(HttpHeaders.AUTHORIZATION,"Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isCreated());

		Movie movie=movieRepository.findByTitle("Om").orElseThrow();

		assertThat(movie.getTitle()).isEqualTo("Om");

	}

	
	@Test 
	public void testApplyTheatreOwner() throws Exception{
		User user=new User();
		user.setName("Aswin");
		user.setPassword(passwordEncoder.encode("Aswin@427"));
		user.setRole(Role.MEMBER);
		user.setEmail("aswin@tcs.com");

		userRepository.save(user);

		String token=jwtUtil.generateToken(user.getName());

		TheatreOwnerDTO theatreOwnerDTO=new TheatreOwnerDTO();
		theatreOwnerDTO.setTheaterName("Vel");
		theatreOwnerDTO.setLocation("Erode");

		String requestjson=objectMapper.writeValueAsString(theatreOwnerDTO);

		mockMvc.perform(post("/auth/applyTheatreOwner").
				header(HttpHeaders.AUTHORIZATION, "Bearer "+token).
				contentType(MediaType.APPLICATION_JSON).
				content(requestjson)).andExpect(status().isOk()).andExpect(content().string("Your Request for theatre owner is pending for approval"));


		TheatreOwnerRequest theatreOwnerRequest=theatreOwnerRequestRepo.findByTheaterName("Vel").orElseThrow();
		assertThat(theatreOwnerRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
		assertThat(theatreOwnerRequest.getLocation()).isEqualTo("Erode");


	}


	@Test 
	public void testAprroveTheatreOwner() throws Exception{

		User user=new User();
		user.setName("Loki");
		user.setPassword(passwordEncoder.encode("Logesh@427"));
		user.setEmail("Logesh@tcs.com");
		user.setRole(Role.ADMIN);
		userRepository.save(user);

		String token=jwtUtil.generateToken(user.getName());

		CommentDTO commentDTO=new CommentDTO();
		commentDTO.setComments("Your request is approved");

		String requestJson=objectMapper.writeValueAsString(commentDTO);

		TheatreOwnerRequest theatreOwnerRequest=new TheatreOwnerRequest();
		theatreOwnerRequest.setTheaterName("Vel");
		theatreOwnerRequest.setLocation("Erode");
		theatreOwnerRequest.setStatus(RequestStatus.PENDING);
		theatreOwnerRequest.setUserId(user.getId());
		theatreOwnerRequestRepo.save(theatreOwnerRequest);
		
		TheatreOwnerRequest requestId=theatreOwnerRequestRepo.findByTheaterName("Vel").orElseThrow();
		Long id=requestId.getId();
		assertThat(id).isNotNull();

		mockMvc.perform(patch("/admin/theatreOwner/approve/{id}",id).
				header(HttpHeaders.AUTHORIZATION, "Bearer "+token).
				contentType(MediaType.APPLICATION_JSON).
				content(requestJson)).
				andExpect(status().isOk()).
				andExpect(content().string("Theatre Owner request approved"));

		Venue venue=venueRepository.findByName("Vel").orElseThrow();
		assertThat(venue.getName()).isEqualTo("Vel");
		assertThat(venue.getLocation()).isEqualTo("Erode");
		assertThat(venue.getOwnerId()).isEqualTo(user.getId());

		User user2=userRepository.findByEmail("Logesh@tcs.com").orElseThrow();
		assertThat(user2.getRole()).isEqualTo(Role.THEATRE_OWNER);
		assertThat(user2.getUsername()).isEqualTo("Loki");

		TheatreOwnerRequest theatreOwnerRequest2=theatreOwnerRequestRepo.findByTheaterName("Vel").orElseThrow();
		assertThat(theatreOwnerRequest2.getStatus()).isEqualTo(RequestStatus.APPROVED);
		assertThat(theatreOwnerRequest2.getComments()).isEqualTo("Your request is approved");

	}

	@Test 
	public void testAddSeat() throws Exception{

		User user=new User();
		user.setName("Loki");
		user.setPassword(passwordEncoder.encode("Logesh@427"));
		user.setEmail("Logesh@tcs.com");
		user.setRole(Role.THEATRE_OWNER);
		userRepository.save(user);

		String token=jwtUtil.generateToken(user.getName());

		User uName=userRepository.findByEmail("Logesh@tcs.com").orElseThrow();

		Venue venue=new Venue();
		venue.setName("Vel");
		venue.setLocation("Erode");
		venue.setOwnerId(uName.getId());
		venueRepository.save(venue);

		Venue vName=venueRepository.findByName("Vel").orElseThrow();
		Long venueId=vName.getId();

		List<RowConfig> seatLists= new ArrayList<>();
		RowConfig rowConfig1=new RowConfig();
		rowConfig1.setRowLabel("A");
		rowConfig1.setSeatCount(20);
		rowConfig1.setSeatType(SeatType.PREMIUM);
		seatLists.add(rowConfig1);

		RowConfig rowConfig2=new RowConfig();
		rowConfig2.setRowLabel("B");
		rowConfig2.setSeatCount(30);
		rowConfig2.setSeatType(SeatType.REGULAR);
		seatLists.add(rowConfig2);
		
		SeatLayoutRequestDTO seatLayoutRequestDTO=new SeatLayoutRequestDTO();
		seatLayoutRequestDTO.setRows(seatLists);

		String requestJson=objectMapper.writeValueAsString(seatLayoutRequestDTO);

		MvcResult response=mockMvc.perform(post("/venue/{id}/seats",venueId).header(HttpHeaders.AUTHORIZATION, "Bearer "+token).
		contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isCreated()).andReturn();

		String responseJson=response.getResponse().getContentAsString();

		List<SeatResponseDTO> seatResponseDTOs=objectMapper.readValue(responseJson, new TypeReference<List<SeatResponseDTO>>() {});

		assertThat(seatResponseDTOs.size()).isEqualTo(50);
		assertThat(seatResponseDTOs).isNotNull();

		List<Seats> seats=seatRepository.findAll();



		assertThat(seats.size()).isEqualTo(50);

	}

	@Test 
	public void testaddShow()throws Exception{

		User user=new User();
		user.setName("Loki");
		user.setPassword(passwordEncoder.encode("Logesh@427"));
		user.setEmail("Logesh@tcs.com");
		user.setRole(Role.THEATRE_OWNER);
		userRepository.save(user);

		String token=jwtUtil.generateToken(user.getName());

		User uName=userRepository.findByEmail("Logesh@tcs.com").orElseThrow();

		Venue venue=new Venue();
		venue.setName("Vel");
		venue.setLocation("Erode");
		venue.setOwnerId(uName.getId());
		venueRepository.save(venue);

		Venue vName=venueRepository.findByName("Vel").orElseThrow();
		Long venueId=vName.getId();

		Seats seats=new Seats();
		seats.setSeatNumber("A15");
		seats.setSeatType(SeatType.PREMIUM);
		seats.setVenue(vName);

		seatRepository.save(seats);

		Seats seats1=new Seats();
		seats1.setSeatNumber("A16");
		seats1.setSeatType(SeatType.PREMIUM);
		seats1.setVenue(vName);

		seatRepository.save(seats1);

		Seats seats2=new Seats();
		seats2.setSeatNumber("A17");
		seats2.setSeatType(SeatType.REGULAR);
		seats2.setVenue(vName);

		seatRepository.save(seats2);

		Movie movie=new Movie();
		movie.setCategory(MovieCategory.COMEDY);
		movie.setCensorRating("U");
		movie.setDescription("Comedy Flim");
		movie.setDuration(123);
		movie.setLanguage("English");
		movie.setPosterUrl("ASDFGHJKL");
		movie.setReleaseDate(LocalDate.of(2026, 12, 12));
		movie.setTitle("Jailer 2");
		
		movieRepository.save(movie);

		Map<SeatType,Double> prices=new HashMap<>();
		prices.put(SeatType.PREMIUM,190.0);
		prices.put(SeatType.REGULAR, 150.0);

		ShowRequestDTO showRequestDTO=new ShowRequestDTO();
		showRequestDTO.setMovieId(movie.getId());
		showRequestDTO.setVenueId(venueId);
		showRequestDTO.setShowDateTime(LocalDateTime.now().plusHours(25));
		showRequestDTO.setPrice(prices);

		String requestJson=objectMapper.writeValueAsString(showRequestDTO);

		mockMvc.perform(post("/venue/shows").header(HttpHeaders.AUTHORIZATION, "Bearer "+token).
		contentType(MediaType.APPLICATION_JSON).
		content(requestJson)).
		andExpect(status().isCreated()).
		andExpect(content().string("Shows and seats for the shows has been created"));

		Shows shows=showsRepository.findByVenue(venue).orElseThrow();

		assertThat(shows.getVenue().getId()).isEqualTo(vName.getId());
		assertThat(shows.getMovie().getId()).isEqualTo(movie.getId());
		

		List<ShowSeat> showSeats=showSeatsRepository.findAll();

		assertThat(showSeats).isNotNull();
		assertThat(showSeats.size()).isEqualTo(3);


	}

}