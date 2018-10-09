#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>

#define RESOURCE_SIZE 50
#define NUM_WRITERS 4
#define NUM_READERS 9

int count;
pthread_mutex_t mx_count;
pthread_mutex_t mx_write;

char resource[RESOURCE_SIZE];


void write_resource(int wid)
{
	printf("w%d-> start\n", wid);

	int i = 0;
	while (i < RESOURCE_SIZE)
	{
		char c = resource[i];
		if (c == '\0') {
			resource[i] = wid + '0';
			break;
		}
		i++;
	}
	
	printf("w%d-> done\n", wid);
}

void read_resource(int rid)
{
	printf("r%d-> read: %s\n", rid, resource);
}

void* writer_sim(void* arg)
{
	int id = *(int*)arg;
	
	pthread_mutex_lock(&mx_write);
	write_resource(id);
	pthread_mutex_unlock(&mx_write);
	
	pthread_exit(0);
}

void* reader_sim(void* arg)
{
	int id = *(int*)arg;
	
	//lock counter mx and increment count
	pthread_mutex_lock(&mx_count);
	count++;
	printf("r%d-> start | r_count: %d\n", id, count);
	if (count == 1)
		pthread_mutex_lock(&mx_write);

	pthread_mutex_unlock(&mx_count);
	
	read_resource(id);
	
	//lock counter mx and decrement count
	pthread_mutex_lock(&mx_count);
	count--;
	printf("r%d-> done\n", id);
	if (count == 0)
		pthread_mutex_unlock(&mx_write);
		
	pthread_mutex_unlock(&mx_count);
		
	pthread_exit(0);
}


int main(int argc, char* argv[])
{
	//Start timer
	struct timeval stop, start;
	gettimeofday(&start, NULL);
	
	//Initialize the resource and mutexes
	int i = 0;
	while (i < RESOURCE_SIZE)
	{
		resource[i] = '\0';
		i++;
	}
	count = 0;
	pthread_mutex_init(&mx_count, NULL);
	pthread_mutex_init(&mx_write, NULL);
	
	//initialize the readers and writers stuff
	pthread_t writers[NUM_WRITERS];
	pthread_t readers[NUM_READERS];

	int wids[NUM_WRITERS] = { 1,2,3,4 };
	int rids[NUM_READERS] = { 1,2,3,4,5,6,7,8,9 };
	int w = 0;
	int r = 0;
	
	//start the writers
	while (w < NUM_WRITERS)
	{
		if (pthread_create(&writers[w], NULL, writer_sim, &wids[w]) != 0)
		{
			printf("Failed to create writer thread!\n");
			exit(EXIT_FAILURE);
		}
		w++;
	}
	
	//start the readers
	while (r < NUM_READERS)
	{
		if (pthread_create(&readers[r], NULL, reader_sim, &rids[r]) != 0)
		{
			printf("Failed to create reader thread!\n");
			exit(EXIT_FAILURE);
		}
		r++;
	}
	
	//wait for writers to finish
	w = 0;
	while (w < NUM_WRITERS) {
		pthread_join(writers[w++], NULL);
	}
	
	//wait for readers to finish
	r = 0;
	while (r < NUM_READERS) {
		pthread_join(readers[r++], NULL);
	}
	
	//destroy the mutexes
	pthread_mutex_destroy(&mx_count);
	pthread_mutex_destroy(&mx_write);
	
	gettimeofday(&stop, NULL);
	printf("Simulation finished. Runtime: %dusec\n", (int)(stop.tv_usec - start.tv_usec));
	return 0;
} 
