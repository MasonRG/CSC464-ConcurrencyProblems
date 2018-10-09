#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>
#include <semaphore.h>
#include <unistd.h>

#define MAX_PASSENGERS 4
#define NUM_PASSENGERS 24

//counters for how many have boarded/unboarded
int boarders;
int unboarders;

//locks to guard the counters
pthread_mutex_t mx_board;
pthread_mutex_t mx_unboard;

//semaphores to queue up and signal fully loaded/unloaded
sem_t board_queue;
sem_t unboard_queue;
sem_t load;
sem_t unload;

//Car thread
void* car_sim(void* arg)
{
	int id = *(int*)arg;
	int i;
	int served = 0;
	printf("Car%d-> start\n", id);
	
	while (served < NUM_PASSENGERS) {
		//Load the car
		printf("Car%d-> load\n", id);
		i = 0;
		while(i < MAX_PASSENGERS) {
			sem_post(&board_queue);
			i++;
		}
		sem_wait(&load);
	
		//Run around the track
		printf("Car%d-> departing\n", id);
		usleep(50000 + rand() % 500000);
		printf("Car%d-> arriving\n", id);
		
		//Unload the car
		i = 0;
		while(i < MAX_PASSENGERS) {
			sem_post(&unboard_queue);
			i++;
		}
		sem_wait(&unload);
		printf("Car%d-> unloaded\n", id);
		
		served += MAX_PASSENGERS;
	}
	
	printf("Car%d-> exit\n", id);
	pthread_exit(0);
}

//Passenger thread
void* passenger_sim(void* arg)
{
	int id = *(int*)arg;
	
	sem_wait(&board_queue);

	pthread_mutex_lock(&mx_board);
	boarders++;
	if (boarders == MAX_PASSENGERS) {
		printf("Pas%d-> last one on\n", id);
		sem_post(&load);
		boarders = 0;
	} else {
		printf("Pas%d-> getting on...\n", id);
	}
	pthread_mutex_unlock(&mx_board);
		
	sem_wait(&unboard_queue);
	
	pthread_mutex_lock(&mx_unboard);
	unboarders++;
	if (unboarders == MAX_PASSENGERS) {
		printf("Pas%d-> last one off\n", id);
		sem_post(&unload);
		unboarders = 0;
	} else {
		printf("Pas%d-> getting off...\n", id);
	}
	pthread_mutex_unlock(&mx_unboard);

	pthread_exit(0);
}


int main(int argc, char* argv[])
{
	//start timer
	struct timeval stop, start;
	gettimeofday(&start, NULL);
	
	//initializations
	boarders = 0;
	unboarders = 0;
	pthread_mutex_init(&mx_board, NULL);
	pthread_mutex_init(&mx_unboard, NULL);
	sem_init(&board_queue, 0, 0);
	sem_init(&unboard_queue, 0, 0);
	sem_init(&load, 0, 0);
	sem_init(&unload, 0, 0);
	
	//initialize the car and passengers threads
	pthread_t car;
	pthread_t passengers[NUM_PASSENGERS];
	int cid = 1;
	int pids[NUM_PASSENGERS];
	
	//start the car
	if (pthread_create(&car, NULL, car_sim, &cid) != 0)
	{
		printf("Failed to create car thread!\n");
		exit(EXIT_FAILURE);
	}
	
	int i = 0;
	//start the passengers
	while (i < NUM_PASSENGERS)
	{
		pids[i] = i+1;
		if (pthread_create(&passengers[i], NULL, passenger_sim, &pids[i]) != 0)
		{
			printf("Failed to create passenger thread!\n");
			exit(EXIT_FAILURE);
		}
		i++;
	}
	
	//wait for car to finish
	pthread_join(car, NULL);
	
	//wait for passengers to finish
	i = 0;
	while (i < NUM_PASSENGERS) {
		pthread_join(passengers[i++], NULL);
	}
	
	//destroy the locks and semaphores
	pthread_mutex_destroy(&mx_board);
	pthread_mutex_destroy(&mx_unboard);
	sem_destroy(&board_queue);
	sem_destroy(&unboard_queue);
	sem_destroy(&load);
	sem_destroy(&unload);
	
	//compute runtime
	gettimeofday(&stop, NULL);
	int elapsed = ((int)(stop.tv_sec - start.tv_sec) * 1000) + ((int)(stop.tv_usec - start.tv_usec) / 1000);
	printf("Simulation finished. Runtime: %dms\n", elapsed);
	return 0;
} 
